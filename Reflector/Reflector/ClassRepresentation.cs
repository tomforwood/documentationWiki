using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Reflector
{
    public class ClassRepresentation : TopLevelDocumentable
    {
        public List<FieldRepresentation> instanceFields = new List<FieldRepresentation>();
        public ClassRepresentation(String name) :
            base("class", name)
        {
        }
    }
}
