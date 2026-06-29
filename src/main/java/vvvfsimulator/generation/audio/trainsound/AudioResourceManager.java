package vvvfsimulator.generation.audio.trainsound;
import createvvvfsim.Configs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
public final class AudioResourceManager{
    public static final String SAMPLE_IR_PATH=Configs.filter_wav;
    public static double[] readResourceAudioFileSample(String path,int[] sampleRateOut){
        try(InputStream in=AudioResourceManager.class.getResourceAsStream(path)){
            if(in==null){
                sampleRateOut[0]=-1;
                return new double[0];
            }
            byte[] bytes=readAllBytes(in);
            return decodePcm16WavMono(bytes,sampleRateOut);
        }
        catch(IOException e){
            sampleRateOut[0]=-1;
            return new double[0];
        }
    }
    public static double[] resampleLinear(double[] input,int inputSampleRate,int outputSampleRate){
        if(input==null || input.length==0 || inputSampleRate<=0 ||
                outputSampleRate<=0 || inputSampleRate==outputSampleRate)
            return input==null?new double[0]:input;
        int outputLength=Math.max(1,(int)Math.round(input.length*(double)outputSampleRate/inputSampleRate));
        double[] output=new double[outputLength];
        double step=(double)inputSampleRate/outputSampleRate;
        for(int i=0;i<outputLength;i++){
            double srcPos=i*step;
            int idx=(int)srcPos;
            double frac=srcPos-idx;
            int idx1=Math.min(idx+1,input.length-1);
            double s0=input[Math.min(idx,input.length-1)];
            double s1=input[idx1];
            output[i]=s0+(s1-s0)*frac;
        }
        return output;
    }
    private static double[] decodePcm16WavMono(byte[] bytes,int[] sampleRateOut){
        if(bytes.length<44 || bytes[0]!='R' || bytes[1]!='I' || bytes[2]!='F' || bytes[3]!='F'){
            sampleRateOut[0]=-1;
            return new double[0];
        }
        int channels=((bytes[23]&0xFF)<<8)|(bytes[22]&0xFF);
        int sampleRate=(bytes[24]&0xFF)|((bytes[25]&0xFF)<<8)|((bytes[26]&0xFF)<<16)|((bytes[27]&0xFF)<<24);
        int bitsPerSample=((bytes[35]&0xFF)<<8)|(bytes[34]&0xFF);
        int dataSize=(bytes[40]&0xFF)|((bytes[41]&0xFF)<<8)|((bytes[42]&0xFF)<<16)|((bytes[43]&0xFF)<<24);
        if(bitsPerSample!=16 || channels==0 || dataSize<=0 || bytes.length<44+dataSize){
            sampleRateOut[0]=-1;
            return new double[0];
        }
        int frames=dataSize/(channels*2);
        double[] out=new double[frames];
        int cursor=44;
        for(int i=0;i<frames;i++){
            int sum=0;
            for(int ch=0;ch<channels;ch++){
                int lo=bytes[cursor++]&0xFF;
                int hi=bytes[cursor++];
                short s=(short)((hi<<8)|lo);
                sum+=s;
            }
            out[i]=(sum/(double)channels)/Short.MAX_VALUE;
        }
        sampleRateOut[0]=sampleRate;
        return out;
    }
    private static byte[] readAllBytes(InputStream in) throws IOException{
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[8192];
        int read;
        while((read=in.read(buffer))!=-1) out.write(buffer,0,read);
        return out.toByteArray();
    }
}