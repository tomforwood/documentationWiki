using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Xml;
using System.Threading.Tasks;

namespace Reflector
{
    public class ClassReflector
    {

        public void reflectClass(ClassRepresentation rep, Type type)
        {
            Type parent = type.BaseType;
            if (parent!=null && parent != typeof(Object)) {
                rep.extensions.Add(parent.FullName);
            }
            foreach (Type t in type.GetInterfaces())
            {
                rep.extensions.Add(t.FullName);
            }


            rep.attributes = getAttributes(type);

            reflectFields(rep, type);
            reflectProperties(rep, type);
            reflectMethods(rep, type);
            reflectConstructors(rep, type);
            reflectSubtypes(rep, type);
        }

        private void reflectSubtypes(ClassRepresentation rep, Type type)
        {
            foreach (Type t in type.GetNestedTypes(BindingFlags.Instance | BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (t.IsNestedPrivate)
                {
                    continue;
                }
                TopLevelDocumentable td = APIReflector.reflectTopOrNested(t);
                rep.nested.Add(td);
            }
        }

        private void reflectConstructors(ClassRepresentation rep, Type type)
        {
            foreach (ConstructorInfo cons in type.GetConstructors(BindingFlags.Instance|BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (cons.IsPrivate || cons.IsAssembly) continue;
                string name = "*constructor*";
                ObjectType returnType = ObjectType.toObjectType(type);
                MethodRepresentation methodRep = new MethodRepresentation(returnType, name);
                methodRep.modifiers.Add(convert(cons));
                methodRep.attributes = getAttributes(cons);
                foreach (ParameterInfo param in cons.GetParameters())
                {
                    ObjectType paramType = ObjectType.toObjectType(param.ParameterType);
                    string paramName = param.Name;
                    methodRep.parameters.Add(new Member(paramType, paramName));
                }
                rep.constructors.Add(methodRep);
            }
        }

        private void reflectProperties(ClassRepresentation rep, Type type)
        {
            foreach (PropertyInfo property in type.GetProperties(BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                PropertyRepresentation proprep = parsePropRep(property);
                if (proprep != null)
                {
                    proprep.modifiers.Add(Member.Modifier.STATIC);
                    rep.staticProperties.Add(proprep);
                }
            }

            foreach (PropertyInfo property in type.GetProperties(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                PropertyRepresentation proprep = parsePropRep(property);
                if (proprep != null) rep.instanceProperties.Add(proprep);
            }
        }

        private PropertyRepresentation parsePropRep(PropertyInfo property)
        {
            string name = property.Name;
            ObjectType propType = ObjectType.toObjectType(property.PropertyType);
            PropertyRepresentation propRep = new PropertyRepresentation(propType, name);
            propRep.attributes = getAttributes(property);
            if (property.DeclaringType != property.ReflectedType)
            {
                propRep.inheritedFrom = property.DeclaringType.FullName;
            }
            propRep.modifiers.Add(convert(property.GetGetMethod(true)));
            if (propRep.modifiers.Contains(Member.Modifier.PRIVATE)) return null;
            if (convert(property.GetGetMethod(true)) >= Member.Modifier.PROTECTED)
            {
                propRep.getter = true;
            }
            if (convert(property.GetSetMethod(true)) >= Member.Modifier.PROTECTED)
            {
                propRep.setter = true;
            }
            return propRep;
        }

        private void reflectFields(ClassRepresentation rep, Type type)
        {
            foreach (FieldInfo field in type.GetFields(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                try {
                    if (field.IsPrivate || field.IsAssembly) continue;
                    string name = field.Name;
                    ObjectType fieldType = ObjectType.toObjectType(field.FieldType);
                    FieldRepresentation fieldRep = new FieldRepresentation(fieldType, name);
                    fieldRep.attributes = getAttributes(field);
                    if (field.DeclaringType != field.ReflectedType)
                    {
                        fieldRep.inheritedFrom = field.DeclaringType.FullName;
                    }
                    convert(fieldRep.modifiers, field);
                    rep.instanceFields.Add(fieldRep);
                }
                catch (TypeLoadException ex) {
                    Console.WriteLine(ex);
                }
            }

            foreach (FieldInfo field in type.GetFields(BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (field.IsPrivate || field.IsAssembly) continue;
                if (field.Name == "OnFlyByWire")
                {
                    getAttributes(field);
                }
                string name = field.Name;
                ObjectType fieldType = ObjectType.toObjectType(field.FieldType);
                FieldRepresentation fieldRep = new FieldRepresentation(fieldType, name);
                fieldRep.attributes = getAttributes(field);
                if (field.DeclaringType != field.ReflectedType)
                {
                    fieldRep.inheritedFrom = field.DeclaringType.FullName;
                }

                convert(fieldRep.modifiers, field);
                if (field.IsLiteral)
                {
                    object val = field.GetRawConstantValue();
                    fieldRep.assignment = val.ToString();
                    fieldRep.modifiers.Add(Member.Modifier.CONSTANT);
                }
                else
                {
                    fieldRep.modifiers.Add(Member.Modifier.STATIC);

                }

                rep.staticFields.Add(fieldRep);
            }
        }

        private static List<String> getAttributes(MemberInfo field)
        {
            List<string> result = new List<string>();
            try
            {
                foreach (CustomAttributeData att in field.GetCustomAttributesData())
                {
                    StringBuilder builder = new StringBuilder();
                    builder.Append("[")
                        .Append(att.AttributeType.Name.Replace("Attribute", ""));
                    String consArgs = String.Join(", ", att.ConstructorArguments.Select(arg => arg.Value.ToString()));
                    String namedArgs = String.Join(", ", att.NamedArguments.Select(arg => arg.ToString()));

                    if (consArgs.Length > 0 || namedArgs.Length > 0)
                    {
                        builder.Append("(");
                        if (consArgs.Length > 0)
                        {
                            builder.Append(consArgs);
                            if (namedArgs.Length > 0)
                            {
                                builder.Append(", ");
                            }
                        }
                        if (namedArgs.Length > 0)
                        {
                            builder.Append(namedArgs);
                        }
                        builder.Append(")");
                    }
                    builder.Append("]");
                    result.Add(builder.ToString());
                }
            }
            catch (Exception ex)
            {

                Console.WriteLine(ex);
            }
            return result.Count() == 0 ? null : result;
        }

        private void reflectMethods(ClassRepresentation rep, Type type)
        {
            foreach (MethodInfo method in type.GetMethods(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                MethodRepresentation methodRep = mrep(method);
                if (methodRep == null) continue;
                rep.instanceMethods.Add(methodRep);
            }

            foreach (MethodInfo method in type.GetMethods(BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                MethodRepresentation methodRep = mrep(method);
                if (methodRep == null) continue;
                methodRep.modifiers.Add(Member.Modifier.STATIC);
                rep.staticMethods.Add(methodRep);
            }
        }

        private MethodRepresentation mrep(MethodInfo method)
        {
            if (method.IsPrivate || method.IsAssembly || method.IsSpecialName) return null;
            string name = method.Name;
            ObjectType returnType = ObjectType.toObjectType(method.ReturnType);
            MethodRepresentation methodRep = new MethodRepresentation(returnType, name);
            if (method.DeclaringType!=method.ReflectedType)
            {
                methodRep.inheritedFrom = method.DeclaringType.FullName;
            }
            if (method.IsGenericMethod)
            {
                Type[] gens = method.GetGenericArguments();
                List<string> genArgs = new List<string>();
                foreach (Type genType in gens)
                {
                    object[] constraints = genType.GetGenericParameterConstraints();
                    string argString = genType.ToString();
                    if (constraints.Count() > 0)
                    {
                        argString+=":"+string.Join(",",constraints);
                    }
                    genArgs.Add(argString);
                }
                methodRep.genericArgs = genArgs;
            }
            methodRep.modifiers.Add(convert(method));
            methodRep.attributes = getAttributes(method);
            if (method.IsVirtual) methodRep.modifiers.Add(Member.Modifier.VIRTUAL);
            foreach (ParameterInfo param in method.GetParameters()) {
                ObjectType paramType = ObjectType.toObjectType(param.ParameterType);
                string paramName = param.Name;
                methodRep.parameters.Add(new Member(paramType, paramName));
            }
            return methodRep;
        }

        private void convert(List<Member.Modifier> list, FieldInfo type)
        {
            if (type.IsPublic)
            {
                list.Add(Member.Modifier.PUBLIC);
            }
            else if (type.IsFamily)
            {
                list.Add(Member.Modifier.PROTECTED);
            }
        }

        private Member.Modifier convert(MethodBase type)
        {
            if (type == null) return Member.Modifier.PRIVATE;
            if (type.IsPublic)
            {
                return Member.Modifier.PUBLIC;
            }
            else if (type.IsPrivate)
            {
                return Member.Modifier.PRIVATE;
            }
            else return Member.Modifier.PROTECTED;
        }
    }
}
