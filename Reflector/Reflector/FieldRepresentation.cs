﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    public class FieldRepresentation : Member
    {

        public string assignment;


        public FieldRepresentation(ObjectType type, String name) :
            base(type, name)
        { }
    }
}
