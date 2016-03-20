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
            IMongoClient client = new MongoClient();
            database = client.GetDatabase("docuWiki");
        }

        public IEnumerable<ClassRepresentation> reflectAssembly(Assembly assembly)
        {
            return assembly.GetExportedTypes().Select(reflectClass);
        }

        public ClassRepresentation reflectClass(Type type)
        {
            ClassRepresentation rep = new ClassRepresentation(type.FullName);
            rep.classModifier = convert(type.Attributes);
            rep.userGenerated = false;
            return rep;
        }

        private ClassRepresentation.Modifier convert(TypeAttributes attributes)
        {
            if (attributes.HasFlag(TypeAttributes.Public)) return ClassRepresentation.Modifier.PUBLIC;
            return ClassRepresentation.Modifier.PROTECTED;
        }

        private void persistRep(ClassRepresentation rep)
        {
            JObject json = rep.getJson();
            string jsonText = json.ToString();
            BsonDocument doc = BsonDocument.Parse(jsonText);
            database.GetCollection<BsonDocument>(collectionName).InsertOne(doc);
        }


        static void Main(string[] args)
        {
            Debug.WriteLine("Hello world");

            Assembly ksp = typeof(GameEvents).Assembly;

            APIReflector reflector = new APIReflector();
            ClassRepresentation rep = reflector.reflectClass(typeof(Part));
            reflector.persistRep(rep);


        }
    }
}
