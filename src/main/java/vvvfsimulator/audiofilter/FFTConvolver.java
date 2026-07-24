package vvvfsimulator.audiofilter;
import java.util.Arrays;
public class FFTConvolver{
    private static volatile Kernel sharedKernel=Kernel.EMPTY;
    private static int sharedVersion;
    private int blockSize;
    private int segSize;
    private int segCount;
    private int fftComplexSize;
    private double[][] segmentsRe=new double[0][];
    private double[][] segmentsIm=new double[0][];
    private Kernel kernel=Kernel.EMPTY;
    private boolean sharedMode;
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
    public static synchronized void updateSharedKernel(int blockSize,double[] ir,int irLen){
        sharedKernel=buildKernel(blockSize,ir,irLen,++sharedVersion);
    }
    public static void ensureSharedKernel(int blockSize,double[] ir,int irLen){
        int normalizedBlockSize=Utilities.NextPowerOf2(blockSize);
        Kernel current=sharedKernel;
        if(current.blockSize!=normalizedBlockSize || (current.segCount==0 && irLen>0))
            updateSharedKernel(blockSize,ir,irLen);
    }
    public boolean init(int blockSize,double[] ir,int irLen){
        reset();
        sharedMode=false;
        Kernel localKernel=buildKernel(blockSize,ir,irLen,0);
        bindKernel(localKernel);
        return blockSize!=0;
    }
    public boolean initShared(int blockSize){
        reset();
        sharedMode=true;
        Kernel currentKernel=sharedKernel;
        if(currentKernel.blockSize!=Utilities.NextPowerOf2(blockSize)) return false;
        bindKernel(currentKernel);
        return true;
    }
    public void process(double[] input,int inputOffset,double[] output,int outputOffset,int len){
        if(sharedMode){
            Kernel currentKernel=sharedKernel;
            if(kernel.version!=currentKernel.version) bindKernel(currentKernel);
        }
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
            fft.fft(fftBuffer,segmentsRe[current],segmentsIm[current]);
            if(inputBufferWasEmpty){
                Arrays.fill(preMultipliedRe,0.0);
                Arrays.fill(preMultipliedIm,0.0);
                for(int i=1;i<segCount;i++){
                    int indexAudio=(current+i)%segCount;
                    Utilities.ComplexMultiplyAccumulate(preMultipliedRe,preMultipliedIm,kernel.segmentsIRRe[i],
                            kernel.segmentsIRIm[i],segmentsRe[indexAudio],segmentsIm[indexAudio],fftComplexSize);
                }
            }
            System.arraycopy(preMultipliedRe,0,convRe,0,fftComplexSize);
            System.arraycopy(preMultipliedIm,0,convIm,0,fftComplexSize);
            Utilities.ComplexMultiplyAccumulate(convRe,convIm,segmentsRe[current],segmentsIm[current],
                    kernel.segmentsIRRe[0],kernel.segmentsIRIm[0],fftComplexSize);
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
    public void clearState(){
        for(double[] segment:segmentsRe) Arrays.fill(segment,0.0);
        for(double[] segment:segmentsIm) Arrays.fill(segment,0.0);
        Arrays.fill(preMultipliedRe,0.0);
        Arrays.fill(preMultipliedIm,0.0);
        Arrays.fill(convRe,0.0);
        Arrays.fill(convIm,0.0);
        Arrays.fill(overlap,0.0);
        Arrays.fill(inputBuffer,0.0);
        Arrays.fill(fftBuffer,0.0);
        current=0;
        inputBufferFill=0;
    }
    public void reset(){
        blockSize=0;
        segSize=0;
        segCount=0;
        fftComplexSize=0;
        segmentsRe=new double[0][];
        segmentsIm=new double[0][];
        kernel=Kernel.EMPTY;
        sharedMode=false;
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
    private void bindKernel(Kernel kernel){
        this.kernel=kernel;
        blockSize=kernel.blockSize;
        segSize=kernel.segSize;
        segCount=kernel.segCount;
        fftComplexSize=kernel.fftComplexSize;
        fft.init(segSize);
        fftBuffer=segSize==0?new double[0]:new double[segSize];
        segmentsRe=new double[segCount][];
        segmentsIm=new double[segCount][];
        for(int i=0;i<segCount;i++){
            segmentsRe[i]=new double[fftComplexSize];
            segmentsIm[i]=new double[fftComplexSize];
        }
        preMultipliedRe=fftComplexSize==0?new double[0]:new double[fftComplexSize];
        preMultipliedIm=fftComplexSize==0?new double[0]:new double[fftComplexSize];
        convRe=fftComplexSize==0?new double[0]:new double[fftComplexSize];
        convIm=fftComplexSize==0?new double[0]:new double[fftComplexSize];
        overlap=blockSize==0?new double[0]:new double[blockSize];
        inputBuffer=blockSize==0?new double[0]:new double[blockSize];
        inputBufferFill=0;
        current=0;
    }
    private static Kernel buildKernel(int blockSize,double[] ir,int irLen,int version){
        if(blockSize==0 || ir==null) return new Kernel(version);
        int normalizedBlockSize=Utilities.NextPowerOf2(blockSize);
        int normalizedSegSize=2*normalizedBlockSize;
        int normalizedFftComplexSize=AudioFFT.ComplexSize(normalizedSegSize);
        irLen=Math.min(irLen,ir.length);
        while(irLen>0 && Math.abs(ir[irLen-1])<1e-6) irLen--;
        if(irLen==0) return new Kernel(version,normalizedBlockSize,normalizedSegSize,0,
                normalizedFftComplexSize,new double[0][],new double[0][]);
        int normalizedSegCount=(int)Math.ceil((double)irLen/normalizedBlockSize);
        double[][] segmentsIRRe=new double[normalizedSegCount][];
        double[][] segmentsIRIm=new double[normalizedSegCount][];
        double[] workBuffer=new double[normalizedSegSize];
        AudioFFT kernelFft=new AudioFFT();
        kernelFft.init(normalizedSegSize);
        for(int i=0;i<normalizedSegCount;i++){
            double[] re=new double[normalizedFftComplexSize];
            double[] im=new double[normalizedFftComplexSize];
            int remaining=irLen-i*normalizedBlockSize;
            int copySize=Math.min(Math.max(remaining,0),normalizedBlockSize);
            Utilities.CopyAndPad(workBuffer,ir,i*normalizedBlockSize,copySize);
            kernelFft.fft(workBuffer,re,im);
            segmentsIRRe[i]=re;
            segmentsIRIm[i]=im;
        }
        return new Kernel(version,normalizedBlockSize,normalizedSegSize,normalizedSegCount,
                normalizedFftComplexSize,segmentsIRRe,segmentsIRIm);
    }
    private static class Kernel{
        private static final Kernel EMPTY=new Kernel(-1);
        private final int version;
        private final int blockSize;
        private final int segSize;
        private final int segCount;
        private final int fftComplexSize;
        private final double[][] segmentsIRRe;
        private final double[][] segmentsIRIm;
        private Kernel(int version){
            this(version,0,0,0,0,new double[0][],new double[0][]);
        }
        private Kernel(int version,int blockSize,int segSize,int segCount,int fftComplexSize,
                       double[][] segmentsIRRe,double[][] segmentsIRIm){
            this.version=version;
            this.blockSize=blockSize;
            this.segSize=segSize;
            this.segCount=segCount;
            this.fftComplexSize=fftComplexSize;
            this.segmentsIRRe=segmentsIRRe;
            this.segmentsIRIm=segmentsIRIm;
        }
    }
}