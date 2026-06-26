package vvvfsimulator.data.trainaudio;
import java.util.ArrayList;
import java.util.List;
import vvvfsimulator.vvvf.model.Motor;
public class Struct{
    public static final int DEFAULT_GEAR1=16;
    public static final int DEFAULT_GEAR2=101;
    public List<HarmonicData> gearSound=new ArrayList<>();
    public List<HarmonicData> harmonicSound=new ArrayList<>();
    public boolean useFilters=false;
    public List<SoundFilter> filters=new ArrayList<>();
    public boolean useConvolutionFilter=true;
    public int impulseResponseSampleRate=192000;
    public double[] impulseResponse=new double[0];
    public Motor.MotorSpecification motorSpec=new Motor.MotorSpecification();
    public double motorVolumeDb=-2.0;
    public double totalVolumeDb=0.0;
    public Struct(){
        this(true);
    }
    private Struct(boolean applyDefaultLists){
        // Align with the original C# defaults so PWM-derived motor sound is audible.
        motorSpec.v=220.0;
        motorSpec.rs=0.45;
        motorSpec.rr=0.38;
        motorSpec.ls=0.012;
        motorSpec.lr=0.012;
        motorSpec.lm=0.011;
        motorSpec.np=2.0;
        motorSpec.inertia=0.2;
        motorSpec.fd=0.001;
        motorSpec.fc=0.001;
        motorSpec.fs=0.01;
        motorSpec.stribeckOmega=2.0;
        motorSpec.fricSmoothK=50.0;
        if(applyDefaultLists) applyOriginalDefaultListParameters();
    }
    public Struct copy(){
        Struct copy=new Struct(false);
        for(HarmonicData h:gearSound) copy.gearSound.add(h.copy());
        for(HarmonicData h:harmonicSound) copy.harmonicSound.add(h.copy());
        for(SoundFilter f:filters) copy.filters.add(f.copy());
        copy.useFilters=useFilters;
        copy.useConvolutionFilter=useConvolutionFilter;
        copy.impulseResponseSampleRate=impulseResponseSampleRate;
        copy.impulseResponse=impulseResponse.clone();
        copy.motorSpec=motorSpec.copy();
        copy.motorVolumeDb=motorVolumeDb;
        copy.totalVolumeDb=totalVolumeDb;
        return copy;
    }
    public void applyOriginalDefaultListParameters(){
        harmonicSound.clear();
        setCalculatedGearHarmonic(DEFAULT_GEAR1,DEFAULT_GEAR2);
    }
    public void setCalculatedGearHarmonic(int gear1,int gear2){
        if(gear2==0){
            gearSound.clear();
            return;
        }
        List<HarmonicData> list=new ArrayList<>();
        double rotation=120.0/Math.pow(2.0,motorSpec.np)/60.0;
        double baseRatio=2.0*gear1/(double)gear2;
        double[] harmonics={9.0*baseRatio*189.0/225,9.0*baseRatio,9.0,1.0};
        for(int i=0;i<harmonics.length;i++){
            HarmonicData data=new HarmonicData();
            data.harmonic=rotation*gear1*harmonics[i];
            data.disappear=-1.0;
            data.amplitude.start=0.0;
            data.amplitude.startValue=0.0;
            data.amplitude.end=40.0;
            data.amplitude.endValue=0.1*Math.pow(1.4,-i);
            data.amplitude.minimumValue=0.0;
            data.amplitude.maximumValue=0.1;
            list.add(data);
        }
        gearSound=list;
    }
    public static class SoundFilter{
        public FilterType type=FilterType.PeakingEQ;
        public float gain;
        public float frequency;
        public float q;
        public SoundFilter copy(){
            SoundFilter copy=new SoundFilter();
            copy.type=type;
            copy.gain=gain;
            copy.frequency=frequency;
            copy.q=q;
            return copy;
        }
        public enum FilterType{
            PeakingEQ,HighPassFilter,LowPassFilter,NotchFilter
        }
    }
    public static class HarmonicData{
        public double harmonic;
        public HarmonicAmplitude amplitude=new HarmonicAmplitude();
        public HarmonicDataRange range=new HarmonicDataRange();
        public double disappear;
        public HarmonicData copy(){
            HarmonicData copy=new HarmonicData();
            copy.harmonic=harmonic;
            copy.amplitude=amplitude.copy();
            copy.range=range.copy();
            copy.disappear=disappear;
            return copy;
        }
        public static class HarmonicAmplitude{
            public double start;
            public double startValue;
            public double end;
            public double endValue;
            public double minimumValue;
            public double maximumValue=0x60;
            public HarmonicAmplitude copy(){
                HarmonicAmplitude copy=new HarmonicAmplitude();
                copy.start=start;
                copy.startValue=startValue;
                copy.end=end;
                copy.endValue=endValue;
                copy.minimumValue=minimumValue;
                copy.maximumValue=maximumValue;
                return copy;
            }
        }
        public static class HarmonicDataRange{
            public double start;
            public double end=-1;
            public HarmonicDataRange copy(){
                HarmonicDataRange copy=new HarmonicDataRange();
                copy.start=start;
                copy.end=end;
                return copy;
            }
        }
    }
}