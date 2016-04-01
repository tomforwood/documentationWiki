using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    class EnumRepresentation : TopLevelDocumentable
    {
        [JsonProperty("enumValues")]
        public List<EnumConstant> enumValues = new List<EnumConstant>();
        public EnumRepresentation(String name) :
            base(new ObjectType("enum"), name)
        {

        }

        public class EnumConstant
        {
            [JsonProperty("name")]
            string name;
            [JsonProperty("enumValue")]
            string value;
            public EnumConstant(string name, string value)
            {
                this.name = name;
                this.value = value;
            }
        }
    }
}
