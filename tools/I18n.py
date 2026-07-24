import json
from io import TextIOWrapper
class I18n:
    sample_dic:dict[str,str]={}
    @staticmethod
    def genSample()->None:
        lang_file:TextIOWrapper=open("lang.json","r",encoding="utf-8")
        lang:dict=json.load(lang_file)
        lang_file.close()
        mod_id:str=lang["mod_id"]
        keys:dict[str,list[str]]=lang["keys"]
        addition:dict[str,str]=lang["addition"]
        for prefix in keys:
            midfixes:list[str]=keys[prefix]
            for midfix in midfixes:
                I18n.sample_dic[mod_id+prefix+midfix]=""
            if prefix in addition:
                suffix:str=addition[prefix]
                for midfix in midfixes:
                    I18n.sample_dic[mod_id+prefix+midfix+suffix]=""
    @staticmethod
    def save(path:str)->None:
        if not path.endswith(".json"): path+=".json"
        sample_file:TextIOWrapper=open(path,"w",encoding="utf-8")
        json.dump(I18n.sample_dic,sample_file,indent=4)
        sample_file.close()