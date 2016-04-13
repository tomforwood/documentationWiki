using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO.Compression;
using Newtonsoft.Json;
using System.IO;

namespace Reflector
{
    internal class FileClassStore : IReflectClassStore
    {
        private string outputFile;
        List<TopLevelDocumentable> list = new List<TopLevelDocumentable>();

        public FileClassStore(string outputFile)
        {
            this.outputFile = outputFile;
        }

        public void storeClass(TopLevelDocumentable doc)
        {
            list.Add(doc);
        }

        public int await()
        {
            FileStream fstream = new FileStream(outputFile + ".gzip", FileMode.Create);
            GZipStream zipStream = new GZipStream(fstream,CompressionLevel.Optimal);
            TextWriter writer = new StreamWriter(zipStream);
            JsonSerializer serializer = new JsonSerializer();
            serializer.Serialize(writer, list);
            return list.Count();
        }
    }
}