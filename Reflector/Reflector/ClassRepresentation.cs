using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Driver;

namespace Reflector
{
    public class ClassRepresentation
    {
        public enum Modifier
        {
            PUBLIC, PROTECTED
        }
        
        public int version;
        public bool userGenerated = false;
        public string namespaceName;
        [JsonConverter(typeof(StringEnumConverter))]
        [BsonRepresentation(BsonType.String)]
        public Modifier classModifier;
        public string name;
        public string fish;

        public ClassRepresentation(String name)
        {
            version = 1;
            this.name = name;
        }

        public JObject getJson()
        {
            JsonSerializer jsonWriter = new JsonSerializer
            {
                NullValueHandling = NullValueHandling.Ignore
            };
            JObject result = JObject.FromObject(this, jsonWriter);
            return result;
        }
    }
}
