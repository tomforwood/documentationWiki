﻿using Newtonsoft.Json;
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
        public List<ObjectType> varargs;

        public List<FieldRepresentation> instanceFields = new List<FieldRepresentation>();
        public List<FieldRepresentation> staticFields = new List<FieldRepresentation>();

        public List<PropertyRepresentation> instanceProperties = new List<PropertyRepresentation>();
        public List<PropertyRepresentation> staticProperties = new List<PropertyRepresentation>();

        public List<MethodRepresentation> instanceMethods = new List<MethodRepresentation>();
        public List<MethodRepresentation> staticMethods = new List<MethodRepresentation>();

        public List<MethodRepresentation> constructors = new List<MethodRepresentation>();

        public List<string> extensions = new List<string>();

        public List<TopLevelDocumentable> nested = new List<TopLevelDocumentable>();

        public ClassRepresentation(String name) :
            base(new ObjectType("class"), name)
        {
        }
    }
}
