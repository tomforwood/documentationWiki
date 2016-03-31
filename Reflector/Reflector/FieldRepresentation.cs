using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    public class FieldRepresentation : Member
    {
        public FieldRepresentation(String type, String name) :
            base(type, name)
        { }

        public string assignment;
    }
}
