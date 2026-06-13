package vvvfsimulator.audiofilter;
import org.jtransforms.fft.DoubleFFT_1D;
public final class AudioFFT{
    private int size;
    private int halfSize;
    private DoubleFFT_1D fft;
    public void init(int size){
        if(size<0) throw new IllegalArgumentException("FFT size must be >= 0");
        if(size!=0 && (size&(size-1))!=0) throw new IllegalArgumentException("FFT size must be power of 2");
        this.size=size;
        this.halfSize=size/2;
        this.fft=size==0?null:new DoubleFFT_1D(size);
    }
    public void fft(double[] data,double[] re,double[] im){
        if(size==0) return;
        if(data.length<size || re.length<halfSize+1 || im.length<halfSize+1)
            throw new IllegalArgumentException("buffer too small for configured FFT size");
        if(size==1){
            re[0]=data[0];
            im[0]=0.0;
            return;
        }
        fft.realForward(data);
        re[0]=data[0];
        im[0]=0.0;
        re[halfSize]=data[1];
        im[halfSize]=0.0;
        for(int k=1;k<halfSize;k++){
            re[k]=data[2*k];
            im[k]=data[2*k+1];
        }
    }
    public void ifft(double[] data,double[] re,double[] im){
        if(size==0) return;
        if(data.length<size || re.length<halfSize+1 || im.length<halfSize+1)
            throw new IllegalArgumentException("buffer too small for configured FFT size");
        if(size==1){
            data[0]=re[0];
            return;
        }
        data[0]=re[0];
        data[1]=re[halfSize];
        for(int k=1;k<halfSize;k++){
            data[2*k]=re[k];
            data[2*k+1]=im[k];
        }
        fft.realInverse(data,true);
    }
    public static int ComplexSize(int size){
        return size/2+1;
    }
}