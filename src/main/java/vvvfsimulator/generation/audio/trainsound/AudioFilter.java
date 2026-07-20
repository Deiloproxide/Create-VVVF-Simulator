package vvvfsimulator.generation.audio.trainsound;
import vvvfsimulator.audiofilter.FFTConvolver;
public class AudioFilter{
    public interface SampleFilter{
        double apply(double input);
    }
    public static class IdentityFilter implements SampleFilter{
        @Override
        public double apply(double input){
            return input;
        }
    }
    public static class CppConvolutionFilter{
        private final FFTConvolver convolver=new FFTConvolver();
        private final double[] input;
        private final double[] output;
        public CppConvolutionFilter(int blockSize,double[] response){
            this.input=new double[blockSize];
            this.output=new double[blockSize];
            convolver.init(blockSize,response,response.length);
        }
        public void reset(){
            convolver.reset();
        }
        public void process(double[] in,int inOffset,double[] out,int outOffset,int len){
            convolver.process(in,inOffset,out,outOffset,len);
        }
        public void process(double[] inOut,int len){
            if(len>input.length) throw new IllegalArgumentException("len exceeds block size");
            System.arraycopy(inOut,0,input,0,len);
            convolver.process(input,0,output,0,len);
            System.arraycopy(output,0,inOut,0,len);
        }
        public static void stereo2monaural(double[] input,int len,double[] outputL,double[] outputR){
            int frames=len/2;
            for(int i=0;i<frames;i++){
                outputL[i]=input[2*i];
                outputR[i]=input[2*i+1];
            }
        }
        public static void monaural2stereo(double[] inputL,double[] inputR,double[] output,int len){
            int frames=len/2;
            for(int i=0;i<frames;i++){
                output[2*i]=inputL[i];
                output[2*i+1]=inputR[i];
            }
        }
    }
}