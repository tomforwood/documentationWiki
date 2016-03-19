using Microsoft.VisualStudio.TestTools.UnitTesting;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Schema;
using Reflector;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector.Tests
{
    [TestClass()]
    public class APIReflectorTests
    {
        [TestMethod()]
        public void reflectClassTest()
        {
            Type t = typeof(ClassRepresentation);
            APIReflector reflector = new APIReflector();
            ClassRepresentation rep = reflector.reflectClass(t);
            JObject json = rep.getJson();
            Debug.WriteLine(json.ToString());

            Uri uri = new Uri("C:/Users/Tom/source/documentationWiki/common/schema/ClassRepresentation.json");
            Validator valid = new Validator(uri);

            IList<ValidationError> errors;
            bool isValid = valid.validate(json, out errors);
            foreach (ValidationError error in errors)
            {
                Debug.WriteLine(error.Path +" : "+error.Message);
            }
            Assert.IsFalse(errors.Any());
            Assert.IsTrue(isValid);
        }

        [TestMethod()]
        public void reflectClassTestFail()
        {
            Type t = typeof(ClassRepresentation);
            APIReflector reflector = new APIReflector();
            ClassRepresentation rep = reflector.reflectClass(t);
            rep.fish = "fishy";
            JObject json = rep.getJson();

            Uri uri = new Uri("C:/Users/Tom/source/documentationWiki/common/schema/ClassRepresentation.json");
            Validator valid = new Validator(uri);

            IList<ValidationError> errors;
            bool isValid = valid.validate(json, out errors);
            foreach (ValidationError error in errors)
            {
                Debug.WriteLine(error.Path + " : " + error.Message);
            }
            Assert.IsTrue(errors.Any());
            Assert.IsFalse(isValid);
        }
    }
}