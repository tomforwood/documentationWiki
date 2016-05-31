using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Xml;
using System.Text;
using Newtonsoft.Json.Linq;
using CommandLine;

namespace Reflector
{
    public class APIReflector
    {
        IReflectClassStore classStore;

        public APIReflector(CLOptions options)
        {
            if (options.outputFile==null)
            {
                classStore = new MongoClassStore(options);
            }
            else
            {
                classStore = new FileClassStore(options.outputFile);
            }   
        }

        public IEnumerable<TopLevelDocumentable> reflectAssembly(Assembly assembly)
        {
            return assembly.GetExportedTypes().Select(reflectTop);
        }

        public static TopLevelDocumentable reflectTop(Type type)
        {
            if (type.IsNested)
            {
                return null;
            }
            return reflectTopOrNested(type);
        }

        /// <summary>
        /// reflects on a Type object (class, enum, struct or interface) (the ones that can be top level
        /// </summary>
        /// <param name="type"></param>
        /// <returns>A representation of that type</returns>
        public static TopLevelDocumentable reflectTopOrNested(Type type)
        {
            if (type.IsGenericType)
            {
                Debug.WriteLine("Generic type found");
                foreach (Type arg in type.GetGenericArguments())
                {
                    Debug.WriteLine(arg.ToString());
                }
            }
            TopLevelDocumentable rep = null;
            if (type.IsEnum)
            {
                rep = new EnumRepresentation(type.FullName);
                reflectEnum((EnumRepresentation)rep, type);
            }
            else if (type.IsClass)
            {
                ObjectType ot = ObjectType.toObjectType(type);
                rep = new ClassRepresentation(ot.typeName);
                ((ClassRepresentation)rep).varargs = ot.varargs;
                new ClassReflector().reflectClass((ClassRepresentation)rep, type);
            }
            else if (type.IsInterface)
            {
                ObjectType ot = ObjectType.toObjectType(type);
                rep = new ClassRepresentation(ot.typeName);
                rep.objectType.typeName = "interface";
                ((ClassRepresentation)rep).varargs = ot.varargs;
                new ClassReflector().reflectClass((ClassRepresentation)rep, type);
            }
            else if (type.IsValueType)
            {
                ObjectType ot = ObjectType.toObjectType(type);
                rep = new ClassRepresentation(ot.typeName);
                rep.objectType.typeName = "struct";
                ((ClassRepresentation)rep).varargs = ot.varargs;
                new ClassReflector().reflectClass((ClassRepresentation)rep, type);
            }

            else
            {
                return null;
            }
            //TypeInfo ti = type..GetTypeInfo();

            rep.userGenerated = false;

            rep.namespaceName = type.Namespace;
            rep.assemblyName = type.Assembly.GetName().Name;
            convert(rep.modifiers, type);
            return rep;
        }
        

        private static void reflectEnum(EnumRepresentation rep, Type type)
        {
            string[] names = type.GetEnumNames();
            Array values = type.GetEnumValues();
            for (int i=0;i<names.Length;i++)
            {
                object o = Convert.ChangeType(values.GetValue(i), type.GetEnumUnderlyingType());
                string value = o.ToString();
                EnumRepresentation.EnumConstant ec = new EnumRepresentation.EnumConstant(names[i], value);
                rep.enumValues.Add(ec);
            }
        }

        private static void convert(List<Member.Modifier> list, Type type)
        {
            if (type.IsPublic) {
                list.Add(Member.Modifier.PUBLIC);
            }
            if (type.IsAbstract && !type.IsInterface)
            {
                list.Add(Member.Modifier.ABSTRACT);
            }
        }

        public void reflectClasses(CLOptions options)
        {
            String assemblyFile = options.assemblyFile;
            Assembly ksp = null;
            if (assemblyFile == null) {
                ksp = typeof(GameEvents).Assembly;
            }
            else
            {
                ksp = Assembly.LoadFrom(assemblyFile);
            }

            foreach (Type type in ksp.GetExportedTypes())
            {
                
                Debug.WriteLine("reflecting " + type.FullName);
                TopLevelDocumentable rep = reflectTop(type);
                if (rep != null)
                {
                    classStore.storeClass(rep);
                }
            }

            ksp = typeof(ConfigNode).Assembly;

            foreach (Type type in ksp.GetExportedTypes())
            {

                Debug.WriteLine("reflecting " + type.FullName);
                TopLevelDocumentable rep = reflectTop(type);
                if (rep != null)
                {
                    classStore.storeClass(rep);
                }
            }

            ksp = typeof(PQSOrbit).Assembly;

            foreach (Type type in ksp.GetExportedTypes())
            {

                Debug.WriteLine("reflecting " + type.FullName);
                TopLevelDocumentable rep = reflectTop(type);
                if (rep != null)
                {
                    classStore.storeClass(rep);
                }
            }

            ksp = typeof(KSPAssets.AssetDefinition).Assembly;

            foreach (Type type in ksp.GetExportedTypes())
            {

                Debug.WriteLine("reflecting " + type.FullName);
                TopLevelDocumentable rep = reflectTop(type);
                if (rep != null)
                {
                    classStore.storeClass(rep);
                }
            }

            int count = classStore.await();

            Debug.WriteLine("inserted =" + count);
        }

        static void Main(string[] args)
        {
            CLOptions options = new CLOptions();
            Parser.Default.ParseArguments(args, options);

            APIReflector reflector = new APIReflector(options);
            reflector.reflectClasses(options);
            
        }
    }
}
