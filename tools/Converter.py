import json,os,struct,subprocess
from io import BufferedReader,BufferedWriter
from subprocess import Popen
class Converter:
    sample_rate:int=-1
    ir_pcm:bytes=b""
    is_error:bool=True
    @staticmethod
    def convert(path:str)->None:
        Converter.is_error=True
        if not os.path.exists(path):
            print(f"File not found: {path}")
            return
        ffprobe_args:list[str]=["ffprobe","-v","quiet",
                                "-print_format","json",
                                "-show_streams","-select_streams",
                                "a:0",path]
        try:
            ffprobe_out:bytes=subprocess.check_output(ffprobe_args)
            stream_data:dict=json.loads(ffprobe_out)["streams"][0]
            Converter.sample_rate=int(stream_data["sample_rate"])
        except Exception as e:
            print(f"Fail to parse: {e}")
            return
        ffmpeg_args:list[str]=["ffmpeg","-v","error","-i",path,
                               "-vn","-ac","1","-ar",
                               str(Converter.sample_rate),
                               "-f", "f32le","-"]
        try:
            tube:Popen[bytes]=subprocess.Popen(ffmpeg_args,
                                  stdout=subprocess.PIPE,stderr=subprocess.PIPE)
            ffmpeg_out:tuple[bytes,bytes]=tube.communicate()
            if(tube.returncode!=0):
                msg:str=ffmpeg_out[1].decode("utf-8")
                print(f"FFmpeg decode failed: {msg}")
                return
            Converter.ir_pcm=ffmpeg_out[0]
            Converter.is_error=False
        except Exception as e:
            print(f"Convert error: {e}")
    @staticmethod
    def saveIR(path:str)->None:
        if Converter.is_error: return
        if not path.endswith(".ir"): path+=".ir"
        header:bytes=struct.pack("<4sI",b"IR\0\0",Converter.sample_rate)
        ir_file:BufferedWriter=open(path,"wb")
        ir_file.write(header)
        ir_file.write(Converter.ir_pcm)
        ir_file.close()
    @staticmethod
    def loadIR(path:str)->None:
        Converter.is_error=True
        if not path.endswith(".ir"): path+=".ir"
        if not os.path.exists(path):
            print(f"File not found: {path}")
            return
        ir_file:BufferedReader=open(path,"rb")
        header:bytes=ir_file.read(8)
        if len(header)<8:
            print(f"Incomplete file: {path}")
            ir_file.close()
            return
        data:tuple=struct.unpack("<4sI",header)
        if data[0]!=b"IR\0\0" or data[1]<=0:
            print(f"Format error: {path}")
            ir_file.close()
            return
        Converter.sample_rate=data[1]
        Converter.ir_pcm=ir_file.read()
        ir_file.close()
        Converter.is_error=False
    @staticmethod
    def saveWav(path:str)->None:
        if Converter.is_error: return
        if not path.endswith(".wav"): path+=".wav"
        channel_num:int=1
        float_size:int=4
        sample_bits:int=32
        block_align:int=channel_num*float_size
        byte_rate:int=Converter.sample_rate*block_align
        data_size:int=len(Converter.ir_pcm)
        riff_size:int=data_size+36
        wav_file:BufferedWriter=open(path,"wb")
        riff:bytes=struct.pack("<4sI4s",b"RIFF",riff_size,b"WAVE")
        wav_file.write(riff)
        fmt:bytes=struct.pack("<4sIHHIIHH",b"fmt ",16,3,channel_num,
                              Converter.sample_rate,byte_rate,block_align,sample_bits)
        wav_file.write(fmt)
        data:bytes=struct.pack("<4sI",b"data",data_size)
        wav_file.write(data)
        wav_file.write(Converter.ir_pcm)
        wav_file.close()
if __name__=="__main__":
    Converter.convert("alt1.wav")
    Converter.saveIR("alt1.ir")