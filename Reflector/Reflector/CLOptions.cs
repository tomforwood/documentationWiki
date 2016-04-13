using CommandLine;
using CommandLine.Text;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Reflector
{
    public class CLOptions
    {
        [Option('o', "outputfile", HelpText = "file to output to instead of database")]
        public string outputFile { get; set; }

        [Option('a', "assemblyfile", HelpText = "Assembly file to be processed")]
        public string assemblyFile { get; set; }

        [Option('p', "password", HelpText = "password for Mongo database")]
        public string password { get; set; }

        [HelpOption]
        public string GetUsage()
        {
            // this without using CommandLine.Text
            var usage = new StringBuilder();
            usage.AppendLine("Quickstart Application 1.0");
            usage.AppendLine("Read user manual for usage instructions...");
            return usage.ToString();
        }
    }
}
