using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Xml;
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
        public APIReflector(string password)
        {
            MongoCredential credential = MongoCredential.CreateMongoCRCredential("docuWiki", "docuWikiUser", password);
            var settings = new MongoClientSettings
            {
                Credentials = new[] { credential }
            };
            settings.Server = new MongoServerAddress("localhost",27018);
            IMongoClient client = new MongoClient(settings);
            //IMongoClient client = new MongoClient();
            database = client.GetDatabase("docuWiki");            
        }

        public IEnumerable<TopLevelDocumentable> reflectAssembly(Assembly assembly)
        {
            return assembly.GetExportedTypes().Select(reflectTop);
        }

        public static TopLevelDocumentable reflectTop(Type type)
        {
            if (type.IsNested)
            {
                return null;
            }
            return reflectTopOrNested(type);
        }

        public static TopLevelDocumentable reflectTopOrNested(Type type)
        {
            if (type.IsGenericType)
            {
                Debug.WriteLine("Generic type found");
                foreach (Type arg in type.GetGenericArguments())
                {
                    Debug.WriteLine(arg.ToString());
                }
            }
            TopLevelDocumentable rep = null;
            if (type.IsEnum)
            {
                rep = new EnumRepresentation(type.FullName);
                reflectEnum((EnumRepresentation)rep, type);
            }
            else if (type.IsClass)
            {
                ObjectType ot = ObjectType.toObjectType(type);
                rep = new ClassRepresentation(ot.typeName);
                ((ClassRepresentation)rep).varargs = ot.varargs;
                new ClassReflector().reflectClass((ClassRepresentation)rep, type);
            }
            else if (type.IsInterface)
            {
                ObjectType ot = ObjectType.toObjectType(type);
                rep = new ClassRepresentation(ot.typeName);
                rep.objectType.typeName = "interface";
                ((ClassRepresentation)rep).varargs = ot.varargs;
                new ClassReflector().reflectClass((ClassRepresentation)rep, type);
            }
            else if (type.IsValueType)
            {
                ObjectType ot = ObjectType.toObjectType(type);
                rep = new ClassRepresentation(ot.typeName);
                rep.objectType.typeName = "struct";
                ((ClassRepresentation)rep).varargs = ot.varargs;
                new ClassReflector().reflectClass((ClassRepresentation)rep, type);
            }

            else
            {
                return null;
            }
            TypeInfo ti = type.GetTypeInfo();

            rep.userGenerated = false;

            rep.namespaceName = type.Namespace;
            convert(rep.modifiers, type);
            return rep;
        }
        

        private static void reflectEnum(EnumRepresentation rep, Type type)
        {
            string[] names = type.GetEnumNames();
            Array values = type.GetEnumValues();
            for (int i=0;i<names.Length;i++)
            {
                object o = Convert.ChangeType(values.GetValue(i), type.GetEnumUnderlyingType());
                string value = o.ToString();
                EnumRepresentation.EnumConstant ec = new EnumRepresentation.EnumConstant(names[i], value);
                rep.enumValues.Add(ec);
            }
        }

        private static void convert(List<Member.Modifier> list, Type type)
        {
            if (type.IsPublic) {
                list.Add(Member.Modifier.PUBLIC);
            }
            if (type.IsAbstract && !type.IsInterface)
            {
                list.Add(Member.Modifier.ABSTRACT);
            }
        }


        private BsonDocument persistRep(TopLevelDocumentable rep)
        {
            JObject json = rep.getJson();
            string jsonText = json.ToString();
            //Debug.WriteLine(jsonText);
            BsonDocument doc = BsonDocument.Parse(jsonText);
            return doc;
        }

        public void reflectClasses()
        {

            Assembly ksp = typeof(GameEvents).Assembly;
            clearCollection();
            int count = 0;
            int batchSize = 0;
            List<BsonDocument> reps = new List<BsonDocument>();
            List<Task> allTasks = new List<Task>();

            foreach (Type type in ksp.GetExportedTypes())
            {
                if (batchSize >= 100)
                {
                    Task task = database.GetCollection<BsonDocument>(collectionName).InsertManyAsync(reps);
                    task.ContinueWith(tasking=> { Debug.WriteLine("Completed a batch"); return ""; });
                    Debug.WriteLine("Persisting batch");
                    allTasks.Add(task);
                    batchSize = 0;
                    reps = new List<BsonDocument>();
                }
                Debug.WriteLine("reflecting " + type.FullName);
                TopLevelDocumentable rep = reflectTop(type);
                if (rep != null)
                {
                    BsonDocument doc = persistRep(rep);
                    reps.Add(doc);
                    count++;
                    batchSize++;
                }
            }
            Task t = database.GetCollection<BsonDocument>(collectionName).InsertManyAsync(reps);
            allTasks.Add(t);

            Task.WaitAll(allTasks.ToArray());

            Debug.WriteLine("inserted =" + count);
            long totalCount = database.GetCollection<BsonDocument>(collectionName).Count(new BsonDocument());
            Debug.WriteLine("total =" + totalCount);
        }

        private void clearCollection()
        {
            database.DropCollection(collectionName);
        }

        static void Main(string[] args)
        {
            string password = "";
            if (args.Length>=1)
            {
                password = args[0];
            }
            APIReflector reflector = new APIReflector(password);
            reflector.reflectClasses();
            
        }
    }
}
