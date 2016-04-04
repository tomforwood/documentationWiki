using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Text;
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

            //TODO I don't understand c# attributes properly
            /*foreach (Attribute a in type.GetCustomAttributes(false))
            {
                rep.annotations.Add(a.ToString());
            }*/

            reflectFields(rep, type);
            reflectProperties(rep, type);
            reflectMethods(rep, type);
            reflectConstructors(rep, type);
            reflectSubtypes(rep, type);
        }

        private void reflectSubtypes(ClassRepresentation rep, Type type)
        {
            if (type.FullName=="Administration")
            {
                Debug.WriteLine("admin nested");
            }
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
            foreach (ConstructorInfo cons in type.GetConstructors(BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (cons.IsPrivate || cons.IsAssembly || cons.IsSpecialName) continue;
                string name = cons.Name;
                ObjectType returnType = ObjectType.toObjectType(type);
                MethodRepresentation methodRep = new MethodRepresentation(returnType, name);
                methodRep.modifiers.Add(convert(cons));
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
            PropertyRepresentation proprep = new PropertyRepresentation(propType, name);
            if (property.DeclaringType != property.ReflectedType)
            {
                proprep.inheritedFrom = property.DeclaringType.FullName;
            }
            proprep.modifiers.Add(convert(property.GetGetMethod(true)));
            if (proprep.modifiers.Contains(Member.Modifier.PRIVATE)) return null;
            if (convert(property.GetGetMethod(true)) >= Member.Modifier.PROTECTED)
            {
                proprep.getter = true;
            }
            if (convert(property.GetSetMethod(true)) >= Member.Modifier.PROTECTED)
            {
                proprep.setter = true;
            }
            return proprep;
        }

        private void reflectFields(ClassRepresentation rep, Type type)
        {
            foreach (FieldInfo field in type.GetFields(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (field.IsPrivate || field.IsAssembly) continue;
                string name = field.Name;
                ObjectType fieldType = ObjectType.toObjectType(field.FieldType);
                FieldRepresentation fieldRep = new FieldRepresentation(fieldType, name);
                if (field.DeclaringType != field.ReflectedType)
                {
                    fieldRep.inheritedFrom = field.DeclaringType.FullName;
                }
                convert(fieldRep.modifiers, field);
                rep.instanceFields.Add(fieldRep);
            }

            foreach (FieldInfo field in type.GetFields(BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (field.IsPrivate || field.IsAssembly) continue;
                string name = field.Name;
                ObjectType fieldType = ObjectType.toObjectType(field.FieldType);
                FieldRepresentation fieldRep = new FieldRepresentation(fieldType, name);
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
            methodRep.modifiers.Add(convert(method));
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
