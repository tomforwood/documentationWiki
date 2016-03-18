using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    public class APIReflector
    {
        static void Main(string[] args)
        {
            Debug.WriteLine("Hello world");

            Assembly ksp = typeof(GameEvents).Assembly;
            //ActionGroupList.
            //ActiveJoint.
            /*foreach (Type type in ksp.GetExportedTypes())
            {
                //type.GetTypeInfo().
                Debug.WriteLine(type.FullName);
            }*/

        }

        public IEnumerable<ClassRepresentation> reflectAssembly(Assembly assembly)
        {
            return assembly.GetExportedTypes().Select(reflectClass);
        }

        public ClassRepresentation reflectClass(Type type)
        {
            ClassRepresentation rep = new ClassRepresentation(type.FullName);
            rep.classModifier = convert(type.Attributes);
            return rep;
        }

        private ClassRepresentation.Modifier convert(TypeAttributes attributes)
        {
            if (attributes.HasFlag(TypeAttributes.Public)) return ClassRepresentation.Modifier.PUBLIC;
            return ClassRepresentation.Modifier.PROTECTED;
        }
    }
}
