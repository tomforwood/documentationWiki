using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    public class MethodRepresentation : Member
    {

        public List<Member> parameters = new List<Member>();

        public MethodRepresentation(ObjectType type, String name) :
            base(type, name)
        { }
    }
}
