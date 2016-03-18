using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Schema;
using Newtonsoft.Json;
using System.IO;
using System.Net;

namespace Reflector
{
    public class Validator
    {
        JSchema schema;

        public Validator(Uri schemaURI)
        {
            using (WebResponse response = WebRequest.Create(schemaURI).GetResponse())
            using (Stream stream = response.GetResponseStream())
            using (TextReader textReader = new StreamReader(stream))
            using (JsonReader reader = new JsonTextReader(textReader))
            {
                JSchemaResolver resolver = new JSchemaUrlResolver();
                JSchemaReaderSettings settings = new JSchemaReaderSettings();
                settings.BaseUri = schemaURI;
                settings.Resolver = resolver;
                schema = JSchema.Load(reader, settings);
            }
        }

        public bool validate(JObject json, out IList<ValidationError> errors)
        {
            return json.IsValid(schema, out errors);
        }
    }
}
