package vvvfsimulator.generation.audio.trainsound;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import loader.LoadContext;
import loader.LoadException;
public class AudioResourceManager{
    public static volatile double[] ir={1.0};
    public static volatile int ir_sample_rate=-1;
    private static final int FORMAT_PCM=0x0001;
    private static final int FORMAT_IEEE_FLOAT=0x0003;
    private static final int FORMAT_EXTENSIBLE=0xFFFE;
    private static final int IR_HEADER_SIZE=8;
    public static double[] resampleLinear(int outputSampleRate){
        if(ir==null || ir.length==0 || ir_sample_rate<=0 ||
                outputSampleRate<=0 || ir_sample_rate==outputSampleRate)
            return ir==null?new double[0]:ir;
        int outputLength=Math.max(1,(int)Math.round(ir.length*(double)outputSampleRate/ir_sample_rate));
        double[] output=new double[outputLength];
        double step=(double)ir_sample_rate/outputSampleRate;
        for(int i=0;i<outputLength;i++){
            double srcPos=i*step;
            int idx=(int)srcPos;
            double frac=srcPos-idx;
            int idx1=Math.min(idx+1,ir.length-1);
            double s0=ir[Math.min(idx,ir.length-1)];
            double s1=ir[idx1];
            output[i]=s0+(s1-s0)*frac;
        }
        ir_sample_rate=outputSampleRate;
        ir=output;
        return output;
    }
    public static LoadContext load(InputStream stream,boolean is_ir){
        byte[] bytes;
        try{
            bytes=readAllBytes(stream);
        }
        catch(IOException ignored){
            return new LoadContext(LoadException.io,0,0);
        }
        if(is_ir) return new LoadContext(decodeIr(bytes),0,0);
        else return new LoadContext(decodeWavMono(bytes),0,0);
    }
    private static byte[] readAllBytes(InputStream in) throws IOException{
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[8192];
        int read;
        while((read=in.read(buffer))!=-1) out.write(buffer,0,read);
        return out.toByteArray();
    }
    private static LoadException decodeIr(byte[] bytes){
        if(bytes.length<=IR_HEADER_SIZE || !fourCC(bytes,0,"IR\0\0")){
            failed();
            return LoadException.irerror;
        }
        int sampleRate=(int)readUInt32(bytes,4,false);
        int dataSize=bytes.length-IR_HEADER_SIZE;
        if(sampleRate<=0 || (dataSize&3)!=0){
            failed();
            return LoadException.irerror;
        }
        int frames=dataSize/4;
        if(frames<=0){
            failed();
            return LoadException.irerror;
        }
        double[] out=new double[frames];
        for(int i=0,offset=IR_HEADER_SIZE;i<frames;i++,offset+=4){
            float sample=Float.intBitsToFloat((int)readUInt32(bytes,offset,false));
            out[i]=Float.isFinite(sample)?sample:0.0;
        }
        ir=out;
        ir_sample_rate=sampleRate;
        return LoadException.normal;
    }
    private static LoadException decodeWavMono(byte[] bytes){
        if(bytes.length<12 || !(fourCC(bytes,0,"RIFF") || fourCC(bytes,0,"RIFX") ||
                fourCC(bytes,0,"RF64")) || !fourCC(bytes,8,"WAVE")){
            failed();
            return LoadException.waverror;
        }
        boolean bigEndian=fourCC(bytes,0,"RIFX");
        WavFormat format=null;
        int dataOffset=-1,dataSize=-1;
        for(int cursor=12;cursor+8<=bytes.length;){
            String id=chunkId(bytes,cursor);
            long chunkSize=readUInt32(bytes,cursor+4,bigEndian);
            int chunkOffset=cursor+8;
            if(chunkSize>bytes.length-chunkOffset) chunkSize=bytes.length-chunkOffset;
            if("fmt ".equals(id)) format=parseFormat(bytes,chunkOffset,(int)chunkSize,bigEndian);
            else if("data".equals(id)){
                dataOffset=chunkOffset;
                dataSize=(int)chunkSize;
            }
            int next=chunkOffset+(int)chunkSize+(int)(chunkSize&1L);
            if(next<=cursor) break;
            cursor=next;
        }
        if(format==null || dataOffset<0 || dataSize<=0){
            failed();
            return LoadException.waverror;
        }
        if(format.audioFormat!=FORMAT_PCM && format.audioFormat!=FORMAT_IEEE_FLOAT){
            failed();
            return LoadException.waverror;
        }
        int bytesPerSample=format.blockAlign/format.channels;
        if(format.channels<=0 || format.sampleRate<=0 || bytesPerSample<=0){
            failed();
            return LoadException.waverror;
        }
        int frames=dataSize/format.blockAlign;
        double[] out=new double[frames];
        for(int i=0;i<frames;i++){
            double sum=0.0;
            int frameOffset=dataOffset+i*format.blockAlign;
            for(int ch=0;ch<format.channels;ch++){
                int sampleOffset=frameOffset+ch*bytesPerSample;
                sum+=decodeSample(bytes,sampleOffset,bytesPerSample,format,bigEndian);
            }
            out[i]=sum/format.channels;
        }
        ir=out;
        ir_sample_rate=format.sampleRate;
        return LoadException.normal;
    }
    private static void failed(){
        ir=new double[]{1.0};
        ir_sample_rate=-1;
    }
    private static WavFormat parseFormat(byte[] bytes,int offset,int size,boolean bigEndian){
        if(size<16) return null;
        int audioFormat=readUInt16(bytes,offset,bigEndian);
        int channels=readUInt16(bytes,offset+2,bigEndian);
        int sampleRate=(int)readUInt32(bytes,offset+4,bigEndian);
        int blockAlign=readUInt16(bytes,offset+12,bigEndian);
        int bitsPerSample=readUInt16(bytes,offset+14,bigEndian);
        if(audioFormat==FORMAT_EXTENSIBLE && size>=40){
            int subFormat=(int)readUInt32(bytes,offset+24,false);
            if(subFormat==FORMAT_PCM || subFormat==FORMAT_IEEE_FLOAT) audioFormat=subFormat;
        }
        return new WavFormat(audioFormat,channels,sampleRate,blockAlign,bitsPerSample);
    }
    private static double decodeSample(byte[] bytes,int offset,int bytesPerSample,
                                       WavFormat format,boolean bigEndian){
        if(format.audioFormat==FORMAT_IEEE_FLOAT){
            if(bytesPerSample==4){
                int bits=(int)readUInt32(bytes,offset,bigEndian);
                float sample=Float.intBitsToFloat(bits);
                return Float.isFinite(sample)?sample:0.0;
            }
            if(bytesPerSample==8){
                long bits=readUInt64(bytes,offset,bigEndian);
                double sample=Double.longBitsToDouble(bits);
                return Double.isFinite(sample)?sample:0.0;
            }
            return 0.0;
        }
        if(format.bitsPerSample==8 && bytesPerSample==1) return ((bytes[offset]&0xFF)-128)/128.0;
        if(bytesPerSample>4 || format.bitsPerSample<=0 || format.bitsPerSample>32) return 0.0;
        long value=readSigned(bytes,offset,bytesPerSample,bigEndian);
        double positiveMax=(1L<<(bytesPerSample*8-1))-1.0;
        return Math.max(-1.0,Math.min(1.0,value/positiveMax));
    }
    private static long readSigned(byte[] bytes,int offset,int size,boolean bigEndian){
        long value=0L;
        if(bigEndian)
            for(int i=0;i<size;i++) value=(value<<8)|(bytes[offset+i]&0xFFL);
        else
            for(int i=0;i<size;i++) value|=(bytes[offset+i]&0xFFL)<<(8*i);
        int bits=size*8;
        long sign=1L<<(bits-1);
        if((value&sign)!=0) value-=1L<<bits;
        return value;
    }
    private static int readUInt16(byte[] bytes,int offset,boolean bigEndian){
        if(bigEndian) return ((bytes[offset]&0xFF)<<8)|(bytes[offset+1]&0xFF);
        return (bytes[offset]&0xFF)|((bytes[offset+1]&0xFF)<<8);
    }
    private static long readUInt32(byte[] bytes,int offset,boolean bigEndian){
        if(bigEndian) return ((bytes[offset]&0xFFL)<<24)|((bytes[offset+1]&0xFFL)<<16)|
                ((bytes[offset+2]&0xFFL)<<8)|(bytes[offset+3]&0xFFL);
        return (bytes[offset]&0xFFL)|((bytes[offset+1]&0xFFL)<<8)|
                ((bytes[offset+2]&0xFFL)<<16)|((bytes[offset+3]&0xFFL)<<24);
    }
    private static long readUInt64(byte[] bytes,int offset,boolean bigEndian){
        long value=0L;
        if(bigEndian)
            for(int i=0;i<8;i++) value=(value<<8)|(bytes[offset+i]&0xFFL);
        else
            for(int i=0;i<8;i++) value|=(bytes[offset+i]&0xFFL)<<(8*i);
        return value;
    }
    private static boolean fourCC(byte[] bytes,int offset,String id){
        if(offset+4>bytes.length) return false;
        for(int i=0;i<4;i++) if(bytes[offset+i]!=(byte)id.charAt(i)) return false;
        return true;
    }
    private static String chunkId(byte[] bytes,int offset){
        return new String(new byte[]{bytes[offset],bytes[offset+1],bytes[offset+2],bytes[offset+3]},
                StandardCharsets.US_ASCII);
    }
    private record WavFormat(int audioFormat,int channels,int sampleRate,int blockAlign,int bitsPerSample){}
}
