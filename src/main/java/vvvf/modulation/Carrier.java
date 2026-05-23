package vvvf.modulation;
import java.util.Random;
import vvvf.MyMath;
import vvvf.model.Struct;
public class Carrier{
    public double angleFrequency=0.0;
    public double time=0.0;
    public boolean useSimpleFrequency=false;
    private RandomFrequency randomInstance=new RandomFrequency();
    private VibratoFrequency vibratoInstance=new VibratoFrequency();
    public double getFrequency(){
        return angleFrequency/MyMath.M_2PI;
    }
    public void setFrequency(double value){
        angleFrequency=MyMath.M_2PI*value;
    }
    public void setAsyncAngleFrequency(double value){
        time=value==0?0:(angleFrequency/value*time);
        angleFrequency=value;
    }
    public void setAsyncFrequency(double value){
        time=value==0?0:(getFrequency()/value*time);
        setFrequency(value);
    }
    public double getPhase(){
        return time*angleFrequency;
    }
    public Carrier copy(){
        Carrier clone=new Carrier();
        clone.angleFrequency=angleFrequency;
        clone.time=time;
        clone.useSimpleFrequency=useSimpleFrequency;
        clone.randomInstance=randomInstance.copy();
        clone.vibratoInstance=vibratoInstance.copy();
        return clone;
    }
    public double calculateBaseCarrierFrequency(double nowTime,Struct.ElectricalParameter electricalState){
        if(electricalState.isNone) return 0;
        Object baseCarrierFrequencyParameter=electricalState.carrierFrequency.baseFrequency;
        if(baseCarrierFrequencyParameter instanceof Struct.ElectricalParameter.CarrierParameter.ConstantFrequency constant)
            return constant.value;
        if(baseCarrierFrequencyParameter instanceof Struct.ElectricalParameter.CarrierParameter.VibratoFrequency vibrato){
            vibratoInstance.setState(useSimpleFrequency,nowTime);
            vibratoInstance.setCustomParameter(vibrato,electricalState.pulsePattern.asyncModulationData.carrierWaveData.vibratoData.baseWave);
            return vibratoInstance.calculate();
        }
        return 0;
    }

    public double calculateCarrierFrequency(double nowTime,Struct.ElectricalParameter electricalState){
        if(electricalState.isNone) return 0;
        double baseCarrierFrequency=calculateBaseCarrierFrequency(nowTime,electricalState);
        randomInstance.setState(useSimpleFrequency,nowTime);
        randomInstance.setCustomParameter(electricalState.carrierFrequency.randomRange,baseCarrierFrequency);
        return randomInstance.calculate();
    }
    public void processCarrierFrequency(double nowTime,Struct.ElectricalParameter electricalState){
        setAsyncFrequency(calculateCarrierFrequency(nowTime,electricalState));
    }
    public void resetIFrequencyTime(double nowTime){
        randomInstance.resetTime(nowTime);
        vibratoInstance.resetTime(nowTime);
    }
    public interface IFrequency{
        void setState(boolean simple,double time);
        double calculate();
        void resetTime(double time);
    }
    public static class RandomFrequency implements IFrequency{
        private boolean simple;
        private double time;
        private Struct.ElectricalParameter.CarrierParameter.RandomFrequency parameter;
        private double baseFrequency;
        private double lastRange;
        private double lastUpdateTime;
        private final Random random=new Random();
        public RandomFrequency copy(){
            RandomFrequency clone=new RandomFrequency();
            clone.simple=simple;
            clone.time=time;
            clone.parameter=parameter==null?null:parameter.copy();
            clone.baseFrequency=baseFrequency;
            clone.lastRange=lastRange;
            clone.lastUpdateTime=lastUpdateTime;
            return clone;
        }
        @Override
        public void setState(boolean simple,double time){
            this.simple=simple;
            this.time=time;
        }
        public void setCustomParameter(Struct.ElectricalParameter.CarrierParameter.RandomFrequency parameter,double baseFrequency){
            this.parameter=parameter;
            this.baseFrequency=baseFrequency;
        }
        @Override
        public double calculate(){
            if(parameter==null) return Double.NaN;
            if(simple) return baseFrequency;
            if(lastUpdateTime+parameter.interval<time){
                double range=random.nextDouble()*parameter.range;
                if(random.nextDouble()<0.5) range=-range;
                lastRange=range;
                lastUpdateTime=time;
            }
            return baseFrequency+lastRange;
        }
        @Override
        public void resetTime(double time){
            this.time=time;
            lastUpdateTime=time;
        }
    }
    public static class VibratoFrequency implements IFrequency{
        private boolean simple;
        private double time;
        private Struct.ElectricalParameter.CarrierParameter.VibratoFrequency parameter;
        private Struct.PulseControl.AsyncControl.CarrierFrequency.VibratoValue.BaseWaveType baseWaveType;
        private double lastInterval;
        private double lastTime;
        public VibratoFrequency copy(){
            VibratoFrequency clone=new VibratoFrequency();
            clone.simple=simple;
            clone.time=time;
            clone.parameter=parameter==null?null:parameter.copy();
            clone.baseWaveType=baseWaveType;
            clone.lastInterval=lastInterval;
            clone.lastTime=lastTime;
            return clone;
        }
        @Override
        public void setState(boolean simple,double time){
            this.simple=simple;
            this.time=time;
        }
        public void setCustomParameter(Struct.ElectricalParameter.CarrierParameter.VibratoFrequency parameter,
                Struct.PulseControl.AsyncControl.CarrierFrequency.VibratoValue.BaseWaveType baseWaveType){
            this.parameter=parameter;
            this.baseWaveType=baseWaveType;
        }
        @Override
        public double calculate(){
            if(parameter==null || baseWaveType==null) return Double.NaN;
            if(simple) return (parameter.highest+parameter.lowest)/2.0;
            if(lastInterval!=parameter.interval){
                lastTime=time-(time-lastTime)*(lastInterval==0?1.0:parameter.interval/lastInterval);
                lastInterval=parameter.interval;
            }
            if(time-lastTime>=parameter.interval) lastTime=time;
            double phase=parameter.interval>0?(time-lastTime)/parameter.interval*MyMath.M_2PI:0;
            double center=(parameter.highest-parameter.lowest)/2.0;
            return switch(baseWaveType){
                case Sine->center*(MyMath.Functions.sine(phase)+1)+parameter.lowest;
                case Triangle->center*(MyMath.Functions.triangle(phase)+1)+parameter.lowest;
                case Square->center*(MyMath.Functions.square(phase)+1)+parameter.lowest;
                case SawUp->center*(MyMath.Functions.saw(phase)+1)+parameter.lowest;
                case SawDown->center*(-MyMath.Functions.saw(phase)+1)+parameter.lowest;
            };
        }
        @Override
        public void resetTime(double time){
            this.time=time;
            lastTime=time;
        }
    }
}