using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MongoDB.Driver;
using MongoDB.Bson;
using Newtonsoft.Json.Linq;

namespace Reflector
{
    class MongoClassStore : IReflectClassStore
    {
        string collectionName = "reflectedClasses";
        protected IMongoDatabase database;
        private CLOptions options;


        List<BsonDocument> reps = new List<BsonDocument>();
        List<Task> allTasks = new List<Task>();
        int count = 0;
        int batchSize = 0;

        public MongoClassStore(CLOptions options)
        {
            IMongoClient client = null;
            if (options.password!=null)
            {
                MongoCredential credential = MongoCredential.CreateMongoCRCredential("docuWiki", "docuWikiUser", options.password);
                var settings = new MongoClientSettings
                {
                    Credentials = new[] { credential }
                };
                settings.Server = new MongoServerAddress("localhost", 27017);
                client = new MongoClient(settings);
            }
            else
            {
                client = new MongoClient();
            }
            database = client.GetDatabase("docuWiki");
            clearCollection();
        }

        public void storeClass(TopLevelDocumentable rep)
        {
            if (batchSize >= 100)
            {
                Task task = database.GetCollection<BsonDocument>(collectionName).InsertManyAsync(reps);
                task.ContinueWith(tasking => { Debug.WriteLine("Completed a batch"); return ""; });
                Debug.WriteLine("Persisting batch");
                allTasks.Add(task);
                batchSize = 0;
                reps = new List<BsonDocument>();
            }

            BsonDocument doc = persistRep(rep);
            reps.Add(doc);
            count++;
            batchSize++;


            throw new NotImplementedException();
        }

        public int await()
        {
            Task t = database.GetCollection<BsonDocument>(collectionName).InsertManyAsync(reps);
            allTasks.Add(t);

            Task.WaitAll(allTasks.ToArray());
            long totalCount = database.GetCollection<BsonDocument>(collectionName).Count(new BsonDocument());


            return (int)totalCount;
        }


        private BsonDocument persistRep(TopLevelDocumentable rep)
        {
            JObject json = rep.getJson();
            string jsonText = json.ToString();
            //Debug.WriteLine(jsonText);
            BsonDocument doc = BsonDocument.Parse(jsonText);
            return doc;
        }

        private void clearCollection()
        {
            database.DropCollection(collectionName);
        }
    }
}
