package vvvfsimulator.audiofilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class FFTConvolver{
    private int blockSize;
    private int segSize;
    private int segCount;
    private int fftComplexSize;
    private final List<double[]> segmentsRe=new ArrayList<>();
    private final List<double[]> segmentsIm=new ArrayList<>();
    private final List<double[]> segmentsIRRe=new ArrayList<>();
    private final List<double[]> segmentsIRIm=new ArrayList<>();
    private double[] fftBuffer=new double[0];
    private final AudioFFT fft=new AudioFFT();
    private double[] preMultipliedRe=new double[0];
    private double[] preMultipliedIm=new double[0];
    private double[] convRe=new double[0];
    private double[] convIm=new double[0];
    private double[] overlap=new double[0];
    private int current;
    private double[] inputBuffer=new double[0];
    private int inputBufferFill;
    public boolean init(int blockSize,double[] ir,int irLen){
        reset();
        if(blockSize==0) return false;
        while(irLen>0 && Math.abs(ir[irLen-1])<1e-6) irLen--;
        if(irLen==0) return true;
        this.blockSize=Utilities.NextPowerOf2(blockSize);
        this.segSize=2*this.blockSize;
        this.segCount=(int)Math.ceil((double)irLen/this.blockSize);
        this.fftComplexSize=AudioFFT.ComplexSize(segSize);
        fft.init(segSize);
        fftBuffer=new double[segSize];
        for(int i=0;i<segCount;i++){
            segmentsRe.add(new double[fftComplexSize]);
            segmentsIm.add(new double[fftComplexSize]);
        }
        for(int i=0;i<segCount;i++){
            double[] re=new double[fftComplexSize];
            double[] im=new double[fftComplexSize];
            int remaining=irLen-i*this.blockSize;
            int copySize=Math.min(Math.max(remaining,0),this.blockSize);
            Utilities.CopyAndPad(fftBuffer,ir,i*this.blockSize,copySize);
            fft.fft(fftBuffer,re,im);
            segmentsIRRe.add(re);
            segmentsIRIm.add(im);
        }
        preMultipliedRe=new double[fftComplexSize];
        preMultipliedIm=new double[fftComplexSize];
        convRe=new double[fftComplexSize];
        convIm=new double[fftComplexSize];
        overlap=new double[this.blockSize];
        inputBuffer=new double[this.blockSize];
        inputBufferFill=0;
        current=0;
        return true;
    }
    public void process(double[] input,int inputOffset,double[] output,int outputOffset,int len){
        if(segCount==0){
            Arrays.fill(output,outputOffset,outputOffset+len,0.0);
            return;
        }
        int processed=0;
        while(processed<len){
            boolean inputBufferWasEmpty=inputBufferFill==0;
            int processing=Math.min(len-processed,blockSize-inputBufferFill);
            int inputBufferPos=inputBufferFill;
            System.arraycopy(input,inputOffset+processed,inputBuffer,inputBufferPos,processing);
            Utilities.CopyAndPad(fftBuffer,inputBuffer,0,blockSize);
            fft.fft(fftBuffer,segmentsRe.get(current),segmentsIm.get(current));
            if(inputBufferWasEmpty){
                Arrays.fill(preMultipliedRe,0.0);
                Arrays.fill(preMultipliedIm,0.0);
                for(int i=1;i<segCount;i++){
                    int indexAudio=(current+i)%segCount;
                    Utilities.ComplexMultiplyAccumulate(preMultipliedRe,preMultipliedIm,segmentsIRRe.get(i),
                            segmentsIRIm.get(i),segmentsRe.get(indexAudio),segmentsIm.get(indexAudio),fftComplexSize);
                }
            }
            System.arraycopy(preMultipliedRe,0,convRe,0,fftComplexSize);
            System.arraycopy(preMultipliedIm,0,convIm,0,fftComplexSize);
            Utilities.ComplexMultiplyAccumulate(convRe,convIm,segmentsRe.get(current),segmentsIm.get(current),
                    segmentsIRRe.get(0),segmentsIRIm.get(0),fftComplexSize);
            fft.ifft(fftBuffer,convRe,convIm);
            Utilities.Sum(output,outputOffset+processed,fftBuffer,
                    inputBufferPos,overlap,inputBufferPos,processing);
            inputBufferFill+=processing;
            if(inputBufferFill==blockSize){
                Arrays.fill(inputBuffer,0.0);
                inputBufferFill=0;
                System.arraycopy(fftBuffer,blockSize,overlap,0,blockSize);
                current=current>0?current-1:segCount-1;
            }
            processed+=processing;
        }
    }
    public void process(double[] input,double[] output,int len){
        process(input,0,output,0,len);
    }
    public void reset(){
        blockSize=0;
        segSize=0;
        segCount=0;
        fftComplexSize=0;
        segmentsRe.clear();
        segmentsIm.clear();
        segmentsIRRe.clear();
        segmentsIRIm.clear();
        fftBuffer=new double[0];
        fft.init(0);
        preMultipliedRe=new double[0];
        preMultipliedIm=new double[0];
        convRe=new double[0];
        convIm=new double[0];
        overlap=new double[0];
        current=0;
        inputBuffer=new double[0];
        inputBufferFill=0;
    }
}