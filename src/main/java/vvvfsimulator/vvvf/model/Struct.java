package vvvfsimulator.vvvf.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.modulation.Carrier;
import vvvfsimulator.vvvf.modulation.DeltaSigma;
public final class Struct{
    public static class Domain{
        public Motor motor;
        public ElectricalParameter electricalState=new ElectricalParameter(2,0);
        private boolean brake;
        private boolean freeRun;
        private double controlFrequency;
        private boolean powerOff;
        private double freeFrequencyChange;
        private boolean allowBaseWaveTimeChange=true;
        private double baseWaveAngleFrequency;
        private double baseWaveTime;
        private double lastT;
        private double t;
        private DeltaSigma[] deltaSigmaInstances=new DeltaSigma[]{
                new DeltaSigma(),new DeltaSigma(),new DeltaSigma()};
        private Carrier carrierInstance=new Carrier();
        public Domain(Motor.MotorSpecification motorSpec){
            this.motor=new Motor(motorSpec);
        }
        public Domain copy(){
            Domain copy=new Domain(motor.specification.copy());
            copy.brake=brake;
            copy.freeRun=freeRun;
            copy.controlFrequency=controlFrequency;
            copy.powerOff=powerOff;
            copy.freeFrequencyChange=freeFrequencyChange;
            copy.allowBaseWaveTimeChange=allowBaseWaveTimeChange;
            copy.baseWaveAngleFrequency=baseWaveAngleFrequency;
            copy.baseWaveTime=baseWaveTime;
            copy.lastT=lastT;
            copy.t=t;
            copy.motor=motor.copy();
            copy.electricalState=electricalState.copy();
            copy.deltaSigmaInstances=new DeltaSigma[]{
                    deltaSigmaInstances[0].copy(),
                    deltaSigmaInstances[1].copy(),
                    deltaSigmaInstances[2].copy()};
            copy.carrierInstance=carrierInstance.copy();
            return copy;
        }
        public double getControlFrequency(){
            return controlFrequency;
        }
        public void setControlFrequency(double controlFrequency){
            this.controlFrequency=controlFrequency;
        }
        public void addControlFrequency(double d){
            this.controlFrequency+=d;
        }
        public boolean isPowerOff(){
            return powerOff;
        }
        public void setPowerOff(boolean powerOff){
            this.powerOff=powerOff;
        }
        public boolean isFreeRun(){
            return freeRun;
        }
        public void setFreeRun(boolean freeRun){
            this.freeRun=freeRun;
        }
        public boolean isBraking(){
            return brake;
        }
        public void setBraking(boolean brake){
            this.brake=brake;
        }
        public boolean isBaseWaveTimeChangeAllowed(){
            return allowBaseWaveTimeChange;
        }
        public void setBaseWaveTimeChangeAllowed(boolean allowBaseWaveTimeChange){
            this.allowBaseWaveTimeChange=allowBaseWaveTimeChange;
        }
        public double getFreeFrequencyChange(){
            return freeFrequencyChange;
        }
        public void setFreeFrequencyChange(double freeFrequencyChange){
            this.freeFrequencyChange=freeFrequencyChange;
        }
        public void processControlParameter(double deltaTime,JerkSettings jerkSettings){
            double maxVoltageFreq;
            JerkSettings.Jerk pattern=isBraking()?jerkSettings.braking:jerkSettings.accelerating;
            if(!isPowerOff()){
                setFreeFrequencyChange(pattern.on.frequencyChangeRate);
                maxVoltageFreq=pattern.on.maxControlFrequency;
                if(isFreeRun() && getControlFrequency()>maxVoltageFreq)
                    setControlFrequency(getBaseWaveFrequency());
            }
            else{
                setFreeFrequencyChange(pattern.off.frequencyChangeRate);
                maxVoltageFreq=pattern.off.maxControlFrequency;
                if(isFreeRun() && getControlFrequency()>maxVoltageFreq)
                    setControlFrequency(maxVoltageFreq);
            }
            if(!isPowerOff()){
                if(!isFreeRun()) setControlFrequency(getBaseWaveFrequency());
                else{
                    double updatedControlFrequency=getControlFrequency()+getFreeFrequencyChange()*deltaTime;
                    if(getBaseWaveFrequency()<=updatedControlFrequency){
                        setControlFrequency(getBaseWaveFrequency());
                        setFreeRun(false);
                    }
                    else{
                        setControlFrequency(updatedControlFrequency);
                        setFreeRun(true);
                    }
                }
            }
            else{
                double freqChange=getFreeFrequencyChange()*deltaTime;
                double finalFreq=getControlFrequency()-freqChange;
                setControlFrequency(Math.max(finalFreq,0));
                setFreeRun(true);
            }
        }
        public void setTimeAll(double time){
            setTime(time);
            setBaseWaveTime(time);
            carrierInstance.time=time;
        }
        public void addTimeAll(double delta){
            addTime(delta);
            addBaseWaveTime(delta);
            carrierInstance.time+=delta;
        }
        public void resetTimeAll(){
            setTimeAll(0);
            lastT=0;
            resetDeltaSigmaInstance();
            carrierInstance.resetIFrequencyTime(0);
        }
        public double getTime(){
            return t;
        }
        public void setTime(double value){
            lastT=t;
            t=value;
        }
        public void addTime(double value){
            setTime(t+value);
        }
        public double getLastTime(){
            return lastT;
        }
        public double getDeltaTime(){
            return t-lastT;
        }
        public double getBaseWaveAngleFrequency(){
            return baseWaveAngleFrequency;
        }
        public void setBaseWaveAngleFrequency(double baseWaveAngleFrequency){
            this.baseWaveAngleFrequency=baseWaveAngleFrequency;
        }
        public double getBaseWaveFrequency(){
            return baseWaveAngleFrequency*MyMath.M_1_2PI;
        }
        public double getBaseWaveTime(){
            return baseWaveTime;
        }
        public void setBaseWaveTime(double baseWaveTime){
            this.baseWaveTime=baseWaveTime;
        }
        public void addBaseWaveTime(double d){
            baseWaveTime+=d;
        }
        public void multiplyBaseWaveTime(double x){
            baseWaveTime*=x;
        }
        public DeltaSigma getDeltaSigmaInstance(int phase){
            return deltaSigmaInstances[phase];
        }
        public void resetDeltaSigmaInstance(){
            deltaSigmaInstances[0].reset();
            deltaSigmaInstances[1].reset();
            deltaSigmaInstances[2].reset();
        }
        public Carrier getCarrierInstance(){
            return carrierInstance;
        }
    }
    public static class ElectricalParameter{
        public boolean isNone=true;
        public boolean isZeroOutput=true;
        public int pwmLevel;
        public PulseControl pulsePattern;
        public CarrierParameter carrierFrequency;
        public Map<PulseControl.Pulse.PulseDataKey,Double> pulseData;
        public double baseWaveFrequency;
        public Double baseWaveAmplitude;
        public ElectricalParameter(int pwmLevel,double baseWaveFrequency){
            this.pwmLevel=pwmLevel;
            this.baseWaveFrequency=baseWaveFrequency;
        }
        public ElectricalParameter(boolean isNone,boolean isZeroOutput,int pwmLevel,PulseControl pulsePattern,CarrierParameter carrierFrequency,Map<PulseControl.Pulse.PulseDataKey,Double> pulseData,double baseWaveFrequency,Double baseWaveAmplitude){
            this.isNone=isNone;
            this.isZeroOutput=isZeroOutput;
            this.pwmLevel=pwmLevel;
            this.pulsePattern=pulsePattern;
            this.carrierFrequency=carrierFrequency;
            this.pulseData=pulseData;
            this.baseWaveFrequency=baseWaveFrequency;
            this.baseWaveAmplitude=baseWaveAmplitude;
        }
        public double getBaseWaveAngleFrequency(){
            return MyMath.M_2PI*baseWaveFrequency;
        }
        public ElectricalParameter copy(){
            return new ElectricalParameter(isNone,isZeroOutput,pwmLevel,pulsePattern==null?null:pulsePattern.copy(),carrierFrequency==null?null:carrierFrequency.copy(),pulseData==null?null:new HashMap<>(pulseData),baseWaveFrequency,baseWaveAmplitude);
        }
        public static class CarrierParameter{
            public RandomFrequency randomRange;
            public Object baseFrequency;
            public CarrierParameter(RandomFrequency randomRange,Object baseFrequency){
                this.randomRange=randomRange;
                this.baseFrequency=baseFrequency;
            }
            public CarrierParameter copy(){
                Object base=baseFrequency;
                if(base instanceof ConstantFrequency constant) base=constant.copy();
                else if(base instanceof VibratoFrequency vibrato) base=vibrato.copy();
                return new CarrierParameter(randomRange.copy(),base);
            }
            public static class RandomFrequency{
                public double range;
                public double interval;
                public RandomFrequency(double range,double interval){
                    this.range=range;
                    this.interval=interval;
                }
                public RandomFrequency copy(){
                    return new RandomFrequency(range,interval);
                }
            }
            public static class ConstantFrequency{
                public double value;
                public ConstantFrequency(double value){
                    this.value=value;
                }
                public ConstantFrequency copy(){
                    return new ConstantFrequency(value);
                }
            }
            public static class VibratoFrequency{
                public double highest;
                public double lowest;
                public double interval;
                public VibratoFrequency(double highest,double lowest,double interval){
                    this.highest=highest;
                    this.lowest=lowest;
                    this.interval=interval;
                }
                public VibratoFrequency copy(){
                    return new VibratoFrequency(highest,lowest,interval);
                }
            }
        }
    }
    public static class PhaseState{
        public int u;
        public int v;
        public int w;
        public PhaseState(int u,int v,int w){
            this.u=u;
            this.v=v;
            this.w=w;
        }
        public static PhaseState zero(){
            return new PhaseState(0,0,0);
        }
        public PhaseState copy(){
            return new PhaseState(u,v,w);
        }
        @Override
        public boolean equals(Object obj){
            if(!(obj instanceof PhaseState other)) return false;
            return u==other.u && v==other.v && w==other.w;
        }
        @Override
        public int hashCode(){
            return Objects.hash(u,v,w);
        }
    }
    public static class PulseControl{
        public double controlFrequencyFrom;
        public double rotateFrequencyFrom=-1;
        public double rotateFrequencyBelow=-1;
        public boolean enableFreeRunOn=true;
        public boolean enableFreeRunOff=true;
        public boolean enableNormal=true;
        public boolean stuckFreeRunOn;
        public boolean stuckFreeRunOff;
        public Pulse pulseMode=new Pulse();
        public AsyncControl asyncModulationData=new AsyncControl();
        public PulseControl copy(){
            PulseControl copy=new PulseControl();
            copy.controlFrequencyFrom=controlFrequencyFrom;
            copy.rotateFrequencyFrom=rotateFrequencyFrom;
            copy.rotateFrequencyBelow=rotateFrequencyBelow;
            copy.enableFreeRunOn=enableFreeRunOn;
            copy.enableFreeRunOff=enableFreeRunOff;
            copy.enableNormal=enableNormal;
            copy.stuckFreeRunOn=stuckFreeRunOn;
            copy.stuckFreeRunOff=stuckFreeRunOff;
            copy.pulseMode=pulseMode.copy();
            copy.asyncModulationData=asyncModulationData.copy();
            return copy;
        }
        public static class Pulse{
            public PulseTypeName pulseType=PulseTypeName.ASYNC;
            public int pulseCount=1;
            public PulseAlternative alternative=PulseAlternative.Default;
            public DiscreteTimeConfiguration discreteTime=new DiscreteTimeConfiguration();
            public BaseWaveType baseWave=BaseWaveType.Sine;
            public List<PulseHarmonic> pulseHarmonics=new ArrayList<>();
            public CarrierWaveConfiguration carrierWave=new CarrierWaveConfiguration();
            public Map<PulseDataKey,PulseDataValue> pulseData=new HashMap<>();
            public Pulse copy(){
                Pulse copy=new Pulse();
                copy.pulseType=pulseType;
                copy.pulseCount=pulseCount;
                copy.alternative=alternative;
                copy.discreteTime=discreteTime.copy();
                copy.baseWave=baseWave;
                copy.carrierWave=carrierWave.copy();
                copy.pulseData=new HashMap<>();
                for(Map.Entry<PulseDataKey,PulseDataValue> e:pulseData.entrySet())
                    copy.pulseData.put(e.getKey(),e.getValue().copy());
                copy.pulseHarmonics=new ArrayList<>();
                for(PulseHarmonic h:pulseHarmonics)
                    copy.pulseHarmonics.add(h.copy());
                return copy;
            }
            public enum PulseTypeName{
                ASYNC,SYNC,CHM,SHE,HO,DELTA_SIGMA
            }
            public enum PulseAlternative{
                Default,CP,Square,Alt1,Alt2,Alt3,Alt4,Alt5,Alt6,Alt7,Alt8,Alt9,
                Alt10,Alt11,Alt12,Alt13,Alt14,Alt15,Alt16,Alt17,Alt18,Alt19,
                Alt20,Alt21,Alt22,Alt23,Alt24,Alt25,Alt26,Alt27,Alt28,Alt29,Alt30
            }
            public enum BaseWaveType{
                Sine,Saw,Square,ModifiedSine1,ModifiedSine2,ModifiedSaw1,
                SV,DPWM30,DPWM60C,DPWM60P,DPWM60N,DPWM120P,DPWM120N
            }
            public enum PulseDataKey{
                Dipolar,PulseWidth,Phase,UpdateFrequency
            }
            public static class DiscreteTimeConfiguration{
                public boolean enabled;
                public int steps=2;
                public DiscreteTimeMode mode=DiscreteTimeMode.Middle;
                public DiscreteTimeConfiguration copy(){
                    DiscreteTimeConfiguration copy=new DiscreteTimeConfiguration();
                    copy.enabled=enabled;
                    copy.steps=steps;
                    copy.mode=mode;
                    return copy;
                }
                public enum DiscreteTimeMode{
                    Left,Middle,Right
                }
            }
            public static class PulseHarmonic{
                public double harmonic=3;
                public boolean isHarmonicProportional=true;
                public double amplitude=0.2;
                public boolean isAmplitudeProportional=true;
                public double initialPhase;
                public PulseHarmonicType type=PulseHarmonicType.Sine;
                public PulseHarmonic copy(){
                    PulseHarmonic copy=new PulseHarmonic();
                    copy.harmonic=harmonic;
                    copy.isHarmonicProportional=isHarmonicProportional;
                    copy.amplitude=amplitude;
                    copy.isAmplitudeProportional=isAmplitudeProportional;
                    copy.initialPhase=initialPhase;
                    copy.type=type;
                    return copy;
                }
                public enum PulseHarmonicType{
                    Sine,Saw,Square
                }
            }
            public static class CarrierWaveConfiguration{
                public CarrierWaveType type=CarrierWaveType.Triangle;
                public CarrierWaveOption option=CarrierWaveOption.FallStart;
                public CarrierWaveConfiguration copy(){
                    CarrierWaveConfiguration copy=new CarrierWaveConfiguration();
                    copy.type=type;
                    copy.option=option;
                    return copy;
                }
                public enum CarrierWaveType{
                    Triangle,Saw,Sine
                }
                public enum CarrierWaveOption{
                    RaiseStart,FallStart,TopStart,BottomStart
                }
            }
            public static class PulseDataValue{
                public PulseDataValueMode mode=PulseDataValueMode.Const;
                public double constant=-1;
                public PulseDataValue copy(){
                    PulseDataValue copy=new PulseDataValue();
                    copy.mode=mode;
                    copy.constant=constant;
                    return copy;
                }
                public enum PulseDataValueMode{
                    Const,Moving
                }
            }
        }
        public static class AsyncControl{
            public RandomModulation randomData=new RandomModulation();
            public CarrierFrequency carrierWaveData=new CarrierFrequency();
            public AsyncControl copy(){
                AsyncControl copy=new AsyncControl();
                copy.randomData=randomData.copy();
                copy.carrierWaveData=carrierWaveData.copy();
                return copy;
            }
            public static class RandomModulation{
                public Parameter range=new Parameter();
                public Parameter interval=new Parameter();
                public RandomModulation copy(){
                    RandomModulation copy=new RandomModulation();
                    copy.range=range.copy();
                    copy.interval=interval.copy();
                    return copy;
                }
                public static class Parameter{
                    public ValueMode mode=ValueMode.Const;
                    public double constant;
                    public Parameter copy(){
                        Parameter copy=new Parameter();
                        copy.mode=mode;
                        copy.constant=constant;
                        return copy;
                    }
                    public enum ValueMode{
                        Const,Moving
                    }
                }
            }
            public static class CarrierFrequency{
                public ValueMode mode=ValueMode.Const;
                public double constant=-1.0;
                public VibratoValue vibratoData=new VibratoValue();
                public CarrierFrequency copy(){
                    CarrierFrequency copy=new CarrierFrequency();
                    copy.mode=mode;
                    copy.constant=constant;
                    copy.vibratoData=vibratoData.copy();
                    return copy;
                }
                public enum ValueMode{
                    Const,Moving,Vibrato,Table
                }
                public static class VibratoValue{
                    public BaseWaveType baseWave=BaseWaveType.SawDown;
                    public VibratoValue copy(){
                        VibratoValue copy=new VibratoValue();
                        copy.baseWave=baseWave;
                        return copy;
                    }
                    public enum BaseWaveType{
                        Sine,Triangle,Square,SawUp,SawDown
                    }
                }
            }
        }
    }
    public static class JerkSettings{
        public Jerk accelerating=new Jerk();
        public Jerk braking=new Jerk();
        public static class Jerk{
            public JerkInfo on=new JerkInfo();
            public JerkInfo off=new JerkInfo();
        }
        public static class JerkInfo{
            public double frequencyChangeRate;
            public double maxControlFrequency;
        }
    }
}