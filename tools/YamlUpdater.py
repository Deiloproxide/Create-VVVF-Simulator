import os
from io import TextIOWrapper
class YamlUpdater:
    @staticmethod
    def update(load_path:str,save_path:str,size:int=32768)->None:
        if not os.path.exists(load_path):
            print(f"File not found: {load_path}")
            return
        saw:str="Saw"
        tri:str="Triangle"
        saw_len:int=len(saw)
        load_file:TextIOWrapper=open(load_path,"r",encoding="utf-8")
        save_file:TextIOWrapper=open(save_path,"w",encoding="utf-8")
        buffer:str=load_file.read(size)
        replaced:str=""
        while buffer:
            replaced=(replaced[1-saw_len:]+buffer).replace(saw,tri)
            save_file.write(replaced[:1-saw_len])
            buffer=load_file.read(size)
        save_file.write(replaced[1-saw_len:])
        load_file.close()
        save_file.close()