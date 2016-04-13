using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    interface IReflectClassStore
    {
        void storeClass(TopLevelDocumentable doc);

        int await();
    }
}
