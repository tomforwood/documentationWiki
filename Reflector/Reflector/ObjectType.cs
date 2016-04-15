using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    public class ObjectType
    {
        public string typeName;
        public List<ObjectType> varargs;

        public static Dictionary<string, string> commonTypes = new Dictionary<string, string>()
        {
            {"System.String","string"}, {"System.Boolean","bool"},
            {"System.Int32", "int"}, {"System.Int64","long"},
            {"System.Object","object"}, {"System.Single", "float"},
            {"System.Double","double"}
        };


        public static ObjectType toObjectType(Type type)
        {
            string name = type.FullName;
            if (name==null) name = type.Name;
            if (commonTypes.ContainsKey(name))
            {
                name = commonTypes[name];
            }
            ObjectType result = new ObjectType(name);
            if (type.IsGenericType)
            {
                if (name.Contains('`'))
                {
                    name= name.Substring(0, name.IndexOf('`'));
                }
                result.typeName = name;
                result.varargs = new List<ObjectType>();
                foreach (Type arg in type.GetGenericArguments())
                {
                    result.varargs.Add(toObjectType(arg));
                }
            }
            return result;
        }

        public ObjectType(String typeName)
        {
            this.typeName = typeName;
        }
    }
}
