package vvvfsimulator.audiofilter;
import java.util.Arrays;
public class TwoStageFFTConvolver{
    private int headBlockSize;
    private int tailBlockSize;
    private final FFTConvolver headConvolver=new FFTConvolver();
    private final FFTConvolver tailConvolver0=new FFTConvolver();
    private double[] tailOutput0=new double[0];
    private double[] tailPrecalculated0=new double[0];
    private final FFTConvolver tailConvolver=new FFTConvolver();
    private double[] tailOutput=new double[0];
    private double[] tailPrecalculated=new double[0];
    private double[] tailInput=new double[0];
    private int tailInputFill;
    private int precalculatedPos;
    private double[] backgroundProcessingInput=new double[0];
    public boolean init(int headBlockSize,int tailBlockSize,double[] ir,int irLen){
        reset();
        if(headBlockSize==0 || tailBlockSize==0) return false;
        headBlockSize=Math.max(1,headBlockSize);
        if(headBlockSize>tailBlockSize){
            int tmp=headBlockSize;
            headBlockSize=tailBlockSize;
            tailBlockSize=tmp;
        }
        while(irLen>0 && Math.abs(ir[irLen-1])<1e-6) irLen--;
        if(irLen==0) return true;
        this.headBlockSize=Utilities.NextPowerOf2(headBlockSize);
        this.tailBlockSize=Utilities.NextPowerOf2(tailBlockSize);
        int headIrLen=Math.min(irLen,this.tailBlockSize);
        headConvolver.init(this.headBlockSize,ir,headIrLen);
        if(irLen>this.tailBlockSize){
            int conv1IrLen=Math.min(irLen-this.tailBlockSize,this.tailBlockSize);
            double[] tailIr0=Arrays.copyOfRange(ir,this.tailBlockSize,this.tailBlockSize+conv1IrLen);
            tailConvolver0.init(this.headBlockSize,tailIr0,conv1IrLen);
            tailOutput0=new double[this.tailBlockSize];
            tailPrecalculated0=new double[this.tailBlockSize];
        }
        if(irLen>2*this.tailBlockSize){
            int tailIrLen=irLen-2*this.tailBlockSize;
            double[] tailIr=Arrays.copyOfRange(ir,2*this.tailBlockSize,2*this.tailBlockSize+tailIrLen);
            tailConvolver.init(this.tailBlockSize,tailIr,tailIrLen);
            tailOutput=new double[this.tailBlockSize];
            tailPrecalculated=new double[this.tailBlockSize];
            backgroundProcessingInput=new double[this.tailBlockSize];
        }
        if(tailPrecalculated0.length>0 || tailPrecalculated.length>0) tailInput=new double[this.tailBlockSize];
        tailInputFill=0;
        precalculatedPos=0;
        return true;
    }
    public void process(double[] input,int inputOffset,double[] output,int outputOffset,int len){
        headConvolver.process(input,inputOffset,output,outputOffset,len);
        if(tailInput.length==0) return;
        int processed=0;
        while(processed<len){
            int remaining=len-processed;
            int processing=Math.min(remaining,headBlockSize-(tailInputFill%headBlockSize));
            int sumBegin=processed;
            int sumEnd=processed+processing;
            if(tailPrecalculated0.length>0){
                int pos=precalculatedPos;
                for(int i=sumBegin;i<sumEnd;i++) output[outputOffset+i]+=tailPrecalculated0[pos++];
            }
            if(tailPrecalculated.length>0){
                int pos=precalculatedPos;
                for(int i=sumBegin;i<sumEnd;i++) output[outputOffset+i]+=tailPrecalculated[pos++];
            }
            System.arraycopy(input,inputOffset+processed,tailInput,tailInputFill,processing);
            tailInputFill+=processing;
            if(tailPrecalculated0.length>0 && tailInputFill%headBlockSize==0){
                int blockOffset=tailInputFill-headBlockSize;
                tailConvolver0.process(tailInput,blockOffset,tailOutput0,blockOffset,headBlockSize);
                if(tailInputFill==tailBlockSize){
                    double[] tmp=tailPrecalculated0;
                    tailPrecalculated0=tailOutput0;
                    tailOutput0=tmp;
                }
            }
            if(tailPrecalculated.length>0 && tailInputFill==tailBlockSize &&
                    backgroundProcessingInput.length==tailBlockSize && tailOutput.length==tailBlockSize){
                double[] tmp=tailPrecalculated;
                tailPrecalculated=tailOutput;
                tailOutput=tmp;
                System.arraycopy(tailInput,0,backgroundProcessingInput,0,tailBlockSize);
                startBackgroundProcessing();
            }
            if(tailInputFill==tailBlockSize){
                tailInputFill=0;
                precalculatedPos=0;
            }
            else precalculatedPos+=processing;
            processed+=processing;
        }
    }
    public void process(double[] input,double[] output,int len){
        process(input,0,output,0,len);
    }
    public void reset(){
        headBlockSize=0;
        tailBlockSize=0;
        headConvolver.reset();
        tailConvolver0.reset();
        tailOutput0=new double[0];
        tailPrecalculated0=new double[0];
        tailConvolver.reset();
        tailOutput=new double[0];
        tailPrecalculated=new double[0];
        tailInput=new double[0];
        tailInputFill=0;
        precalculatedPos=0;
        backgroundProcessingInput=new double[0];
    }
    protected void startBackgroundProcessing(){
        doBackgroundProcessing();
    }
    protected void doBackgroundProcessing(){
        if(backgroundProcessingInput.length==tailBlockSize && tailOutput.length==tailBlockSize)
            tailConvolver.process(backgroundProcessingInput,tailOutput,tailBlockSize);
    }
}