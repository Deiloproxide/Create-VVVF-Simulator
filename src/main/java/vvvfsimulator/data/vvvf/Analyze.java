package vvvfsimulator.data.vvvf;
import java.util.List;
import vvvfsimulator.data.vvvf.Struct.AmplitudeValue.Parameter;
import vvvfsimulator.data.vvvf.Struct.AmplitudeValue.Parameter.ValueMode;
import vvvfsimulator.data.vvvf.Struct.AsyncControlEx.CarrierFrequencyEx.TableValue;
import vvvfsimulator.data.vvvf.Struct.AsyncControlEx.CarrierFrequencyEx.VibratoValueEx;
import vvvfsimulator.data.vvvf.Struct.FunctionValue;
import vvvfsimulator.data.vvvf.Struct.FreqAmp;
import vvvfsimulator.data.vvvf.Struct.PulseControlEx;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.model.Config;
import vvvfsimulator.vvvf.model.Struct.Domain;
import vvvfsimulator.vvvf.model.Struct.ElectricalParameter;
import vvvfsimulator.vvvf.model.Struct.ElectricalParameter.CarrierParameter;
import vvvfsimulator.vvvf.model.Struct.JerkSettings.Jerk;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.CarrierFrequency;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.RandomModulation;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
public final class Analyze{
    private static final PulseDataKey[] PULSE_DATA_KEYS=PulseDataKey.values();
    private static double getChangingValue(double x1,double y1,double x2,double y2,double x){
        return y1+(y2-y1)/(x2-x1)*(x-x1);
    }
    private static double getMovingValue(FunctionValue data,double current){
        return switch(data.type){
            case Proportional->getChangingValue(data.start,data.startValue,data.end,data.endValue,current);
            case Pow2_Exponential->
                    (Math.pow(2,Math.pow((current-data.start)/(data.end-data.start),data.degree))-1)*
                            (data.endValue-data.startValue)+data.startValue;
            case Inv_Proportional->{
                double x=getChangingValue(data.start,1.0/data.startValue,data.end,1.0/data.endValue,current);
                double c=-data.curveRate;
                double k=data.endValue;
                double l=data.startValue;
                double a=1.0/((1.0/l)-(1.0/k))*(1.0/(l-c)-1.0/(k-c));
                double b=1.0/(1.0-(1.0/l)*k)*(1.0/(l-c)-(1.0/l)*k/(k-c));
                yield 1.0/(a*x+b)+c;
            }
            case Sine->{
                double x=(MyMath.M_PI_2-Math.asin(data.startValue/data.endValue))/(data.end-data.start)*
                        (current-data.start)+Math.asin(data.startValue/data.endValue);
                yield Math.sin(x)*data.endValue;
            }
        };
    }
    private static double getAmplitude(Parameter param,double current){
        return getAmplitude(param,current,param.startFrequency,param.startAmplitude,
                param.endFrequency,param.endAmplitude);
    }
    private static double getAmplitude(Parameter param,double current,double startFrequency,double startAmplitude,
                                       double endFrequency,double endAmplitude){
        if(param.mode==ValueMode.Table){
            if(param.amplitudeTable.length==0) return 0;
            int target=0;
            for(int i=0;i<param.amplitudeTable.length;i++){
                if(param.amplitudeTable[i].frequency>current) break;
                target=i;
            }
            if(param.amplitudeTableInterpolation && target+1<param.amplitudeTable.length){
                FreqAmp a=param.amplitudeTable[target];
                FreqAmp b=param.amplitudeTable[target+1];
                return (b.amplitude-a.amplitude)/(b.frequency-a.frequency)*(current-a.frequency)+a.amplitude;
            }
            return param.amplitudeTable[target].amplitude;
        }
        double amplitude;
        if(endAmplitude==startAmplitude) amplitude=startAmplitude;
        else if(param.mode==ValueMode.Linear){
            if(!param.disableRangeLimit){
                current=Math.max(current,startFrequency);
                current=Math.min(current,endFrequency);
            }
            amplitude=(endAmplitude-startAmplitude)/(endFrequency-startFrequency)*
                    (current-startFrequency)+startAmplitude;
        }
        else if(param.mode==ValueMode.InverseProportional){
            if(!param.disableRangeLimit){
                current=Math.max(current,startFrequency);
                current=Math.min(current,endFrequency);
            }
            double x=(1.0/endAmplitude-1.0/startAmplitude)/(endFrequency-startFrequency)*
                    (current-startFrequency)+1.0/startAmplitude;
            double c=-param.curveChangeRate;
            double k=endAmplitude;
            double l=startAmplitude;
            double a=1.0/(1.0/l-1.0/k)*(1.0/(l-c)-1.0/(k-c));
            double b=1.0/(1.0-1.0/l*k)*(1.0/(l-c)-1.0/l*k/(k-c));
            amplitude=1.0/(a*x+b)+c;
        }
        else if(param.mode==ValueMode.Exponential){
            if(!param.disableRangeLimit) current=Math.min(current,endFrequency);
            double t=1.0/endFrequency*Math.log(endAmplitude+1);
            amplitude=Math.exp(t*current)-1;
        }
        else if(param.mode==ValueMode.LinearPolynomial){
            if(!param.disableRangeLimit) current=Math.min(current,endFrequency);
            amplitude=Math.pow(current,param.polynomial)/Math.pow(endFrequency,param.polynomial)*
                    endAmplitude;
        }
        else{
            if(!param.disableRangeLimit) current=Math.min(current,endFrequency);
            amplitude=Math.sin(Math.PI*current/(2.0*endFrequency))*endAmplitude;
        }
        if(param.cutOffAmplitude>amplitude) amplitude=0;
        if(param.maxAmplitude!=-1 && param.maxAmplitude<amplitude) amplitude=param.maxAmplitude;
        return amplitude;
    }
    private static boolean isMatching(Domain domain,PulseControlEx ysd){
        boolean freeRunCond=domain.isFreeRun() &&
                ((!domain.isPowerOff() && ysd.enableFreeRunOn) || (domain.isPowerOff() && ysd.enableFreeRunOff));
        boolean normalCond=ysd.enableNormal && !domain.isFreeRun();
        if(!(freeRunCond || normalCond)) return false;
        boolean cond1=ysd.controlFrequencyFrom<=domain.getControlFrequency();
        boolean cond2=ysd.rotateFrequencyFrom==-1 || ysd.rotateFrequencyFrom<=domain.getBaseWaveFrequency();
        boolean cond3=ysd.rotateFrequencyBelow==-1 || ysd.rotateFrequencyBelow>domain.getBaseWaveFrequency();
        if(!cond2 || !cond3) return false;
        if(!domain.isFreeRun()) return cond1;
        if(cond1) return true;
        if((ysd.stuckFreeRunOn && !domain.isPowerOff()) || (ysd.stuckFreeRunOff && domain.isPowerOff()))
            return domain.getBaseWaveFrequency()>ysd.controlFrequencyFrom;
        return false;
    }
    public static void calculate(Domain domain,Struct config){
        ElectricalParameter electricalState=domain.electricalState;
        double minBaseFrequency=domain.isBraking()?config.minimumFrequency.braking:
                config.minimumFrequency.accelerating;
        if(0<domain.getControlFrequency() && domain.getControlFrequency()<minBaseFrequency && !domain.isFreeRun())
            domain.setControlFrequency(minBaseFrequency);
        domain.setBaseWaveTimeChangeAllowed(!(domain.getBaseWaveFrequency()<minBaseFrequency &&
                domain.getControlFrequency()>0));
        double solvedBaseWaveFrequency=domain.isBaseWaveTimeChangeAllowed()?domain.getBaseWaveFrequency():
                minBaseFrequency;
        List<PulseControlEx> source=domain.isBraking()?config.brakingPattern:config.acceleratePattern;
        int solveIndex=-1;
        for(int i=0;i<source.size();i++){
            if(isMatching(domain,source.get(i))){
                solveIndex=i;
                break;
            }
        }
        if(solveIndex==-1){
            if(domain.isFreeRun()){
                if(domain.isPowerOff()) domain.setControlFrequency(0);
                else domain.setControlFrequency(domain.getBaseWaveFrequency());
            }
            electricalState.setNone(config.level,solvedBaseWaveFrequency);
            return;
        }
        PulseControlEx solvePattern=source.get(solveIndex);
        Pulse solvePulse=solvePattern.pulseMode;
        CarrierParameter solvedCarrierFrequency=null;
        if(solvePulse.pulseType==Pulse.PulseTypeName.ASYNC){
            var randomData=solvePattern.asyncModulationDataEx.randomData;
            CarrierParameter carrierParameter=electricalState.carrierParameterCache;
            carrierParameter.randomRange.range=randomData.range.mode==RandomModulation.Parameter.ValueMode.Moving?
                    getMovingValue(randomData.range.movingValue,domain.getControlFrequency()):
                    randomData.range.constant;
            carrierParameter.randomRange.interval=randomData.interval.mode==RandomModulation.Parameter.ValueMode.Moving?
                    getMovingValue(randomData.interval.movingValue,domain.getControlFrequency()):
                    randomData.interval.constant;
            Object baseFrequency;
            if(solvePattern.asyncModulationDataEx.carrierWaveData.mode==CarrierFrequency.ValueMode.Vibrato){
                VibratoValueEx vib=solvePattern.asyncModulationDataEx.carrierWaveData.vibratoData;
                CarrierParameter.VibratoFrequency vibrato=electricalState.vibratoFrequencyCache;
                vibrato.highest=vib.highest.mode==VibratoValueEx.ParameterEx.ValueMode.Moving?
                        getMovingValue(vib.highest.movingValue,domain.getControlFrequency()):vib.highest.constant;
                vibrato.lowest=vib.lowest.mode==VibratoValueEx.ParameterEx.ValueMode.Moving?
                        getMovingValue(vib.lowest.movingValue,domain.getControlFrequency()):vib.lowest.constant;
                vibrato.interval=vib.interval.mode==VibratoValueEx.ParameterEx.ValueMode.Moving?
                        getMovingValue(vib.interval.movingValue,domain.getControlFrequency()):vib.interval.constant;
                baseFrequency=vibrato;
            }
            else if(solvePattern.asyncModulationDataEx.carrierWaveData.mode==CarrierFrequency.ValueMode.Table){
                List<TableValue.Parameter> table=
                        solvePattern.asyncModulationDataEx.carrierWaveData.carrierFrequencyTable.table;
                TableValue.Parameter target=table.isEmpty()?null:table.get(0);
                for(TableValue.Parameter p:table){
                    boolean flag1=p.freeRunStuckAtHere && domain.getBaseWaveFrequency()>=p.controlFrequencyFrom &&
                            domain.isFreeRun();
                    boolean flag2=domain.getControlFrequency()>p.controlFrequencyFrom;
                    if(flag1 || flag2){
                        target=p;
                        break;
                    }
                }
                CarrierParameter.ConstantFrequency constant=electricalState.constantFrequencyCache;
                constant.value=target==null?0:target.carrierFrequency;
                baseFrequency=constant;
            }
            else if(solvePattern.asyncModulationDataEx.carrierWaveData.mode==CarrierFrequency.ValueMode.Moving){
                CarrierParameter.ConstantFrequency constant=electricalState.constantFrequencyCache;
                constant.value=getMovingValue(solvePattern.asyncModulationDataEx.carrierWaveData.movingValue,
                        domain.getControlFrequency());
                baseFrequency=constant;
            }
            else{
                CarrierParameter.ConstantFrequency constant=electricalState.constantFrequencyCache;
                constant.value=solvePattern.asyncModulationDataEx.carrierWaveData.constant;
                baseFrequency=constant;
            }
            carrierParameter.baseFrequency=baseFrequency;
            solvedCarrierFrequency=carrierParameter;
        }
        double solvedAmplitude;
        if(domain.isFreeRun()){
            Jerk baseJerk=domain.isBraking()?config.jerkSetting.braking:config.jerkSetting.accelerating;
            Parameter param=domain.isPowerOff()?solvePattern.amplitude.powerOff:solvePattern.amplitude.powerOn;
            double startFrequency=param.startFrequency;
            double startAmplitude=param.startAmplitude;
            double endFrequency=param.endFrequency;
            double endAmplitude=param.endAmplitude;
            double maxControlFrequency=!domain.isPowerOff()?
                    baseJerk.on.maxControlFrequency:baseJerk.off.maxControlFrequency;
            if(endFrequency==-1){
                if(solvePattern.amplitude.defaultValue.disableRangeLimit)
                    endFrequency=domain.getBaseWaveFrequency();
                else{
                    endFrequency=Math.min(domain.getBaseWaveFrequency(),maxControlFrequency);
                    endFrequency=Math.min(endFrequency,solvePattern.amplitude.defaultValue.endFrequency);
                }
            }
            if(endAmplitude==-1)
                endAmplitude=getAmplitude(solvePattern.amplitude.defaultValue,domain.getBaseWaveFrequency());
            if(startAmplitude==-1)
                startAmplitude=getAmplitude(solvePattern.amplitude.defaultValue,domain.getBaseWaveFrequency());
            solvedAmplitude=getAmplitude(param,domain.getControlFrequency(),
                    startFrequency,startAmplitude,endFrequency,endAmplitude);
        }
        else solvedAmplitude=getAmplitude(solvePattern.amplitude.defaultValue,domain.getControlFrequency());
        double[] solvedPulseData=electricalState.pulseDataCache;
        for(PulseDataKey key:PULSE_DATA_KEYS)
            solvedPulseData[key.ordinal()]=Config.getPulseDataKeyDefaultConstant(key);
        PulseDataKey[] keys=Config.getAvailablePulseDataKey(solvePulse,config.level);
        for(PulseDataKey key:keys){
            double value=solvePulse.pulseData!=null && solvePulse.pulseData.containsKey(key)?
                    solvePulse.pulseData.get(key).constant:Config.getPulseDataKeyDefaultConstant(key);
            solvedPulseData[key.ordinal()]=value;
        }
        if(domain.isPowerOff() && solvedAmplitude==0) domain.setControlFrequency(0);
        boolean isZeroOutput=electricalState.hasBaseWaveAmplitude &&
                (electricalState.baseWaveAmplitude==0 || domain.getControlFrequency()==0);
        electricalState.isNone=false;
        electricalState.isZeroOutput=isZeroOutput;
        electricalState.pwmLevel=config.level;
        electricalState.pulsePattern=solvePattern;
        electricalState.carrierFrequency=solvedCarrierFrequency;
        electricalState.pulseData=solvedPulseData;
        electricalState.baseWaveFrequency=solvedBaseWaveFrequency;
        electricalState.hasBaseWaveAmplitude=true;
        electricalState.baseWaveAmplitude=solvedAmplitude;
    }
}