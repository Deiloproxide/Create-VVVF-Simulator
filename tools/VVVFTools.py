from argparse import _SubParsersAction,ArgumentParser,Namespace
from Converter import Converter
from I18n import I18n
from YamlUpdater import YamlUpdater
class VVVFTools:
    @staticmethod
    def main()->None:
        parser:ArgumentParser=ArgumentParser(description="Create VVVF Simulator Tools",
            epilog="Copyright (C) 2007 Free Software Foundation under GNU GPLv3")
        command:_SubParsersAction=parser.add_subparsers(dest="command",required=True)
        convert:ArgumentParser=command.add_parser("convert",help="convert audio and ir format")
        convert.add_argument("--direction","-d",required=True,choices=["ir","wav"])
        convert.add_argument("--input","-i",required=True,help="input file")
        convert.add_argument("--output","-o",default="converted",help="output file")
        i18n:ArgumentParser=command.add_parser("generate",help="generate language sample file")
        i18n.add_argument("--output","-o",default="sample.json",help="output file")
        updater:ArgumentParser=command.add_parser("update",help="update yaml from older version")
        updater.add_argument("--input","-i",required=True,help="input file")
        updater.add_argument("--output","-o",default="updated.yaml",help="output file")
        updater.add_argument("--size","-s",type=int,default=32768,help="read buffer size")
        args:Namespace=parser.parse_args()
        if args.command=="convert":
            if args.direction=="ir":
                Converter.convert(args.input)
                Converter.saveIR(args.output)
            elif args.direction=="wav":
                Converter.loadIR(args.input)
                Converter.saveWav(args.output)
        elif args.command=="generate":
            I18n.genSample()
            I18n.save(args.output)
        elif args.command=="update":
            YamlUpdater.update(args.input,args.output,args.size)
if __name__=="__main__":
    VVVFTools.main()