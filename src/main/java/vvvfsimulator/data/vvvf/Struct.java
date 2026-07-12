package vvvfsimulator.data.vvvf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import vvvfsimulator.data.Util;
import vvvfsimulator.vvvf.model.Config;
import vvvfsimulator.vvvf.model.Struct.PulseControl;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.CarrierFrequency.VibratoValue.BaseWaveType;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.CarrierFrequency;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.RandomModulation.Parameter.ValueMode;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataValue;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataValue.PulseDataValueMode;
import vvvfsimulator.vvvf.model.Struct.JerkSettings;
public class Struct{
    public int level=2;
    public JerkSettings jerkSetting=new JerkSettings();
    public MinimumBaseFrequency minimumFrequency=new MinimumBaseFrequency();
    public List<PulseControlEx> acceleratePattern=new ArrayList<>();
    public List<PulseControlEx> brakingPattern=new ArrayList<>();
    @Override
    public String toString(){
        return Util.getPropertyValues(this);
    }
    public void sortAcceleratePattern(boolean inverse){
        acceleratePattern.sort((a,b)->
                Double.compare(inverse?a.controlFrequencyFrom:b.controlFrequencyFrom,
                        inverse?b.controlFrequencyFrom:a.controlFrequencyFrom));
    }
    public void sortBrakingPattern(boolean inverse){
        brakingPattern.sort((a,b)->
                Double.compare(inverse?a.controlFrequencyFrom:b.controlFrequencyFrom,
                        inverse?b.controlFrequencyFrom:a.controlFrequencyFrom));
    }
    public void sortForRuntime(){
        sortAcceleratePattern(false);
        sortBrakingPattern(false);
        sortCarrierFrequencyTables(acceleratePattern);
        sortCarrierFrequencyTables(brakingPattern);
    }
    private static void sortCarrierFrequencyTables(List<PulseControlEx> patterns){
        for(PulseControlEx control:patterns)
            control.asyncModulationDataEx.carrierWaveData.carrierFrequencyTable.table.sort((a,b)->
                    Double.compare(b.controlFrequencyFrom,a.controlFrequencyFrom));
    }
    public boolean hasCustomPwm(){
        List<PulseControlEx> all=new ArrayList<>(acceleratePattern);
        all.addAll(brakingPattern);
        for(PulseControlEx control:all){
            Pulse.PulseTypeName type=control.pulseMode.pulseType;
            if(type==Pulse.PulseTypeName.CHM || type==Pulse.PulseTypeName.SHE) return true;
        }
        return false;
    }
    public static class MinimumBaseFrequency{
        public double accelerating=-1.0;
        public double braking=-1.0;
    }
    public static class FunctionValue{
        public FunctionType type=FunctionType.Proportional;
        public double start=0;
        public double startValue=0;
        public double end=1;
        public double endValue=100;
        public double degree=2;
        public double curveRate=0;
        public FunctionValue copy(){
            FunctionValue copy=new FunctionValue();
            copy.type=type;
            copy.start=start;
            copy.startValue=startValue;
            copy.end=end;
            copy.endValue=endValue;
            copy.degree=degree;
            copy.curveRate=curveRate;
            return copy;
        }
        public enum FunctionType{
            Proportional,Inv_Proportional,Pow2_Exponential,Sine
        }
    }
    public static class PulseControlEx extends PulseControl{
        public AmplitudeValue amplitude=new AmplitudeValue();
        public AsyncControlEx asyncModulationDataEx=new AsyncControlEx();
        public PulseControlEx copyEx(){
            PulseControlEx copy=new PulseControlEx();
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
            copy.amplitude=amplitude.copy();
            copy.asyncModulationDataEx=asyncModulationDataEx.copy();
            return copy;
        }
    }
    public static class AsyncControlEx{
        public RandomModulationEx randomData=new RandomModulationEx();
        public CarrierFrequencyEx carrierWaveData=new CarrierFrequencyEx();
        public AsyncControlEx copy(){
            AsyncControlEx copy=new AsyncControlEx();
            copy.randomData=randomData.copy();
            copy.carrierWaveData=carrierWaveData.copy();
            return copy;
        }
        public static class RandomModulationEx{
            public ParameterEx range=new ParameterEx();
            public ParameterEx interval=new ParameterEx();
            public RandomModulationEx copy(){
                RandomModulationEx copy=new RandomModulationEx();
                copy.range=range.copy();
                copy.interval=interval.copy();
                return copy;
            }
            public static class ParameterEx{
                public ValueMode mode=ValueMode.Const;
                public double constant=0;
                public FunctionValue movingValue=new FunctionValue();
                public ParameterEx copy(){
                    ParameterEx copy=new ParameterEx();
                    copy.mode=mode;
                    copy.constant=constant;
                    copy.movingValue=movingValue.copy();
                    return copy;
                }
            }
        }
        public static class CarrierFrequencyEx{
            public CarrierFrequency.ValueMode mode=CarrierFrequency.ValueMode.Const;
            public double constant=-1.0;
            public FunctionValue movingValue=new FunctionValue();
            public VibratoValueEx vibratoData=new VibratoValueEx();
            public TableValue carrierFrequencyTable=new TableValue();
            public CarrierFrequencyEx copy(){
                CarrierFrequencyEx copy=new CarrierFrequencyEx();
                copy.mode=mode;
                copy.constant=constant;
                copy.movingValue=movingValue.copy();
                copy.vibratoData=vibratoData.copy();
                copy.carrierFrequencyTable=carrierFrequencyTable.copy();
                return copy;
            }
            public static class VibratoValueEx{
                public ParameterEx highest=new ParameterEx();
                public ParameterEx lowest=new ParameterEx();
                public ParameterEx interval=new ParameterEx();
                public BaseWaveType baseWave=BaseWaveType.Triangle;
                public VibratoValueEx copy(){
                    VibratoValueEx copy=new VibratoValueEx();
                    copy.highest=highest.copy();
                    copy.lowest=lowest.copy();
                    copy.interval=interval.copy();
                    copy.baseWave=baseWave;
                    return copy;
                }
                public static class ParameterEx{
                    public ValueMode mode=ValueMode.Const;
                    public double constant=-1;
                    public FunctionValue movingValue=new FunctionValue();
                    public ParameterEx copy(){
                        ParameterEx copy=new ParameterEx();
                        copy.mode=mode;
                        copy.constant=constant;
                        copy.movingValue=movingValue.copy();
                        return copy;
                    }
                    public enum ValueMode{
                        Const,Moving
                    }
                }
            }
            public static class TableValue{
                public List<Parameter> table=new ArrayList<>();
                public TableValue copy(){
                    TableValue copy=new TableValue();
                    for(Parameter parameter:table) copy.table.add(parameter.copy());
                    return copy;
                }
                public static class Parameter{
                    public double controlFrequencyFrom=-1;
                    public double carrierFrequency=1000;
                    public boolean freeRunStuckAtHere=false;
                    public Parameter copy(){
                        Parameter copy=new Parameter();
                        copy.controlFrequencyFrom=controlFrequencyFrom;
                        copy.carrierFrequency=carrierFrequency;
                        copy.freeRunStuckAtHere=freeRunStuckAtHere;
                        return copy;
                    }
                }
            }
        }
    }
    public static class AmplitudeValue{
        public Parameter defaultValue=new Parameter();
        public Parameter powerOn=new Parameter();
        public Parameter powerOff=new Parameter();
        public AmplitudeValue copy(){
            AmplitudeValue copy=new AmplitudeValue();
            copy.defaultValue=defaultValue.copy();
            copy.powerOn=powerOn.copy();
            copy.powerOff=powerOff.copy();
            return copy;
        }
        public static class Parameter{
            public ValueMode mode=ValueMode.Linear;
            public double startFrequency=-1;
            public double startAmplitude=-1;
            public double endFrequency=-1;
            public double endAmplitude=-1;
            public double curveChangeRate=0;
            public double cutOffAmplitude=-1;
            public double maxAmplitude=-1;
            public boolean disableRangeLimit=false;
            public double polynomial=0;
            public boolean amplitudeTableInterpolation=false;
            public FreqAmp[] amplitudeTable=new FreqAmp[0];
            public Parameter copy(){
                Parameter copy=new Parameter();
                copy.mode=mode;
                copy.startFrequency=startFrequency;
                copy.startAmplitude=startAmplitude;
                copy.endFrequency=endFrequency;
                copy.endAmplitude=endAmplitude;
                copy.curveChangeRate=curveChangeRate;
                copy.cutOffAmplitude=cutOffAmplitude;
                copy.maxAmplitude=maxAmplitude;
                copy.disableRangeLimit=disableRangeLimit;
                copy.polynomial=polynomial;
                copy.amplitudeTableInterpolation=amplitudeTableInterpolation;
                copy.amplitudeTable=Arrays.stream(amplitudeTable).map(FreqAmp::copy).toArray(FreqAmp[]::new);
                return copy;
            }
            public enum ValueMode{
                Linear,LinearPolynomial,InverseProportional,Exponential,Sine,Table
            }
        }
    }
    public static class FreqAmp{
        public double frequency;
        public double amplitude;
        public FreqAmp(double frequency,double amplitude){
            this.frequency=frequency;
            this.amplitude=amplitude;
        }
        public FreqAmp copy(){
            return new FreqAmp(frequency,amplitude);
        }
    }
    public static class PulseDataValueEx{
        public PulseDataValueMode mode=PulseDataValueMode.Const;
        public double constant=-1;
        public FunctionValue movingValue=new FunctionValue();
        public PulseDataValueEx copy(){
            PulseDataValueEx copy=new PulseDataValueEx();
            copy.mode=mode;
            copy.constant=constant;
            copy.movingValue=movingValue.copy();
            return copy;
        }
    }
    public static void ensurePulseDataDefaults(PulseControlEx pattern){
        Pulse pulse=pattern.pulseMode;
        if(pulse.pulseData==null) pulse.pulseData=new HashMap<>();
        for(PulseDataKey key:Config.getAvailablePulseDataKey(pulse,2))
            pulse.pulseData.putIfAbsent(key,new PulseDataValue());
    }
}