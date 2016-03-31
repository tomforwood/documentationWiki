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
        public APIReflector()
        {
            /*MongoCredential credential = MongoCredential.CreateMongoCRCredential("docuWiki", "docuWikiUser", "***REMOVED***");
            var settings = new MongoClientSettings
            {
                Credentials = new[] { credential }
            };
            IMongoClient client = new MongoClient(settings);*/
            IMongoClient client = new MongoClient();
            database = client.GetDatabase("docuWiki");            
        }

        public IEnumerable<TopLevelDocumentable> reflectAssembly(Assembly assembly)
        {
            return assembly.GetExportedTypes().Select(reflectTop);
        }

        public TopLevelDocumentable reflectTop(Type type)
        {
            TopLevelDocumentable rep = null;
            if (type.IsEnum)
            {
                rep = new EnumRepresentation(type.FullName);
                reflectEnum((EnumRepresentation)rep, type);
            }
            else if (type.IsClass)
            {
                rep = new ClassRepresentation(type.FullName);
                reflectClass((ClassRepresentation)rep, type);
            }
            else
            {
                return null;
            }
            TypeInfo ti = type.GetTypeInfo();

            rep.userGenerated = false;

            rep.namespaceName = type.Namespace;
            convert(rep.modifiers,type);
            return rep;
        }

        private void reflectClass(ClassRepresentation rep, Type type)
        {
            foreach (FieldInfo field in type.GetFields(BindingFlags.Instance |BindingFlags.Public| BindingFlags.NonPublic)) {
                if (field.IsPrivate) continue;
                string name = field.Name;
                string fieldType = field.FieldType.Name;
                FieldRepresentation fieldRep = new FieldRepresentation(fieldType, name);
                convert(fieldRep.modifiers, field);
                if (name== "configTabIndent")
                {

                    Debug.WriteLine(field.Attributes);
                }
                if (field.Attributes.ToString().Contains("HasDefault"))
                {
                    Debug.WriteLine(field.Attributes);
                    //Consts will have this set I believe
                }
                //object value = field.GetValue(Activator.CreateInstance(type));
                //fieldRep.assignment = "=" + value;
                rep.instanceFields.Add(fieldRep);

            }
        }

        private void reflectEnum(EnumRepresentation rep, Type type)
        {
            string[] names = type.GetEnumNames();
            Array values = type.GetEnumValues();
            Debug.WriteLine(type.GetEnumUnderlyingType().FullName);
            for (int i=0;i<names.Length;i++)
            {
                object o = Convert.ChangeType(values.GetValue(i), type.GetEnumUnderlyingType());
                string value = o.ToString();
                EnumRepresentation.EnumConstant ec = new EnumRepresentation.EnumConstant(names[i], value);
                rep.enumValues.Add(ec);
            }
        }

        private void convert(List<Member.Modifier> list, Type type)
        {
            if (type.IsPublic) {
                list.Add(ClassRepresentation.Modifier.PUBLIC);
            }
            if (type.IsAbstract)
            {
                list.Add(ClassRepresentation.Modifier.ABSTRACT);
            }
        }
        private void convert(List<Member.Modifier> list, FieldInfo type)
        {
            if (type.Name=="agent")
            {
                Debug.Write("hello");
            }
            if (type.IsPublic)
            {
                list.Add(ClassRepresentation.Modifier.PUBLIC);
            }
            else if (type.IsFamily)
            {
                list.Add(ClassRepresentation.Modifier.PROTECTED);
            }
        }


        private BsonDocument persistRep(TopLevelDocumentable rep)
        {
            JObject json = rep.getJson();
            string jsonText = json.ToString();
            Debug.WriteLine(jsonText);
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

            APIReflector reflector = new APIReflector();
            reflector.reflectClasses();
            
        }
    }
}
