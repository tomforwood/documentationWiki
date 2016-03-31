using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;

namespace Reflector
{
    public class Member
    {
        public enum Modifier
        {
            PUBLIC, PROTECTED,
            ABSTRACT
        }

        public string objectType;

        [JsonProperty("modifiers", ItemConverterType = typeof(StringEnumConverter))]
        [BsonRepresentation(BsonType.String)]
        public List<Modifier> modifiers = new List<Modifier>();

        public string name;

        public Member(string objectType, string name)
        {
            this.objectType = objectType;
            this.name = name;
        }
    }
}
