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

            foreach (FieldInfo field in type.GetFields(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (field.IsPrivate) continue;
                string name = field.Name;
                ObjectType fieldType = ObjectType.toObjectType(field.FieldType);
                FieldRepresentation fieldRep = new FieldRepresentation(fieldType, name);
                convert(fieldRep.modifiers, field);
                rep.instanceFields.Add(fieldRep);
            }

            foreach (FieldInfo field in type.GetFields(BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (field.IsPrivate) continue;
                string name = field.Name;
                ObjectType fieldType = ObjectType.toObjectType(field.FieldType);
                FieldRepresentation fieldRep = new FieldRepresentation(fieldType, name);
                convert(fieldRep.modifiers, field);
                fieldRep.modifiers.Add(Member.Modifier.STATIC);
                rep.staticFields.Add(fieldRep);
            }

            foreach (PropertyInfo property in type.GetProperties(BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                string name = property.Name;
                ObjectType propType = ObjectType.toObjectType(property.PropertyType);
                PropertyRepresentation proprep = new PropertyRepresentation(propType, name);
                if (property.Name == "GameTime")
                {
                    Console.WriteLine(property);
                }
                proprep.modifiers.Add(convert(property.GetGetMethod(true)));
                if (proprep.modifiers.Contains(Member.Modifier.PRIVATE)) continue;
                proprep.modifiers.Add(Member.Modifier.STATIC);
                if (convert(property.GetGetMethod(true))>=Member.Modifier.PROTECTED)
                {
                    proprep.getter = true;
                }
                if (convert(property.GetSetMethod(true)) >= Member.Modifier.PROTECTED)
                {
                    proprep.setter = true;
                }
                rep.staticProperties.Add(proprep);
            }

            foreach (PropertyInfo property in type.GetProperties(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                string name = property.Name;
                ObjectType propType = ObjectType.toObjectType(property.PropertyType);
                PropertyRepresentation proprep = new PropertyRepresentation(propType, name);
                proprep.modifiers.Add(convert(property.GetGetMethod(true)));
                if (proprep.modifiers.Contains(Member.Modifier.PRIVATE)) continue;
                if (property.GetGetMethod(true)!=null)
                {
                    proprep.getter = true;
                }
                if (property.GetSetMethod(true) != null)
                {
                    proprep.setter = true;
                }
                rep.instanceProperties.Add(proprep);
            }
        }

        private void reflectEnum(EnumRepresentation rep, Type type)
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

        private void convert(List<Member.Modifier> list, Type type)
        {
            if (type.IsPublic) {
                list.Add(Member.Modifier.PUBLIC);
            }
            if (type.IsAbstract)
            {
                list.Add(Member.Modifier.ABSTRACT);
            }
        }

        private void convert(List<Member.Modifier> list, FieldInfo type)
        {
            if (type.IsPublic)
            {
                list.Add(Member.Modifier.PUBLIC);
            }
            else if (type.IsFamily)
            {
                list.Add(Member.Modifier.PROTECTED);
            }
        }

        private Member.Modifier convert(MethodInfo type)
        {
            if (type == null) return ClassRepresentation.Modifier.PRIVATE;
            if (type.IsPublic)
            {
                return Member.Modifier.PUBLIC;
            }
            else if (type.IsPrivate)
            {
                return Member.Modifier.PRIVATE;
            }
            else return Member.Modifier.PROTECTED;
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
