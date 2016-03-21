using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using MongoDB.Driver;
using MongoDB.Bson;
using Newtonsoft.Json.Linq;

namespace Reflector
{
    public class APIReflector
    {
        protected IMongoDatabase database;
        string collectionName = "reflectedClasses";
        public APIReflector()
        {
            MongoCredential credential = MongoCredential.CreateMongoCRCredential("docuWiki", "docuWikiUser", "***REMOVED***");
            var settings = new MongoClientSettings
            {
                Credentials = new[] { credential }
            };
            IMongoClient client = new MongoClient(settings);
            database = client.GetDatabase("docuWiki");            
        }

        public IEnumerable<ClassRepresentation> reflectAssembly(Assembly assembly)
        {
            return assembly.GetExportedTypes().Select(reflectClass);
        }

        public ClassRepresentation reflectClass(Type type)
        {
            ClassRepresentation rep = new ClassRepresentation(type.Name);
            rep.classModifiers.Add(convert(type.Attributes));
            rep.userGenerated = false;
            rep.namespaceName = type.Namespace;
            return rep;
        }

        private ClassRepresentation.Modifier convert(TypeAttributes attributes)
        {
            //TODO this isn't right in any way
            if (attributes.HasFlag(TypeAttributes.Public)) return ClassRepresentation.Modifier.PUBLIC;
            return ClassRepresentation.Modifier.PROTECTED;
        }

        private void persistRep(ClassRepresentation rep)
        {
            JObject json = rep.getJson();
            string jsonText = json.ToString();
            Debug.WriteLine(jsonText);
            BsonDocument doc = BsonDocument.Parse(jsonText);
            database.GetCollection<BsonDocument>(collectionName).InsertOne(doc);
        }


        static void Main(string[] args)
        {
            Debug.WriteLine("Hello world");

            Assembly ksp = typeof(GameEvents).Assembly;

            APIReflector reflector = new APIReflector();
            reflector.clearCollection();
            int count = 0;
            foreach (Type type in ksp.GetExportedTypes()) {
                ClassRepresentation rep = reflector.reflectClass(type);
                reflector.persistRep(rep);
                count++;
            }
            Debug.WriteLine("inserted =" + count);
            long totalCount = reflector.database.GetCollection<BsonDocument>(reflector.collectionName).Count(new BsonDocument());
            Debug.WriteLine("total =" + totalCount);
            int i = reflector[0];
        }

        public int this[int i]
        {
            get { return 100; }
        }

        private void clearCollection()
        {
            database.DropCollection(collectionName);
        }
    }
}
