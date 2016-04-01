using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    public class PropertyRepresentation : Member
    {
        public bool setter;
        public bool getter;
        public PropertyRepresentation(ObjectType type, String name) :
            base(type, name)
        { }
    }
}
