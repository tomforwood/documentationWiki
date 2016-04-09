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
            PRIVATE=0,
            PROTECTED=1,
            PUBLIC=2,
            ABSTRACT=4,
            STATIC=8,
            CONSTANT = 16,
            VIRTUAL = 32,
        }

        public ObjectType objectType;

        [JsonProperty("modifiers", ItemConverterType = typeof(StringEnumConverter))]
        [BsonRepresentation(BsonType.String)]
        public List<Modifier> modifiers = new List<Modifier>();

        public string name;

        public List<string> attributes = new List<string>();
        //TODO attributes have rich structure

        public string inheritedFrom;

        public Member(ObjectType objectType, string name)
        {
            this.objectType = objectType;
            this.name = name;
        }
    }
}
