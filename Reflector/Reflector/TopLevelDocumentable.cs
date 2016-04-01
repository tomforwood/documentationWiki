using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;

namespace Reflector
{
    public class TopLevelDocumentable : Member
    {

        public int version;

        public bool userGenerated;

        public string namespaceName;

        public TopLevelDocumentable(ObjectType objectType, string name) :
            base(objectType,name)
        {
            version = 1;
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
