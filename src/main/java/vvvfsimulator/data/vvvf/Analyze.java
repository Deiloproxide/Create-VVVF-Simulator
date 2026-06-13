package vvvfsimulator.data.vvvf;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import vvvfsimulator.vvvf.model.Struct.ElectricalParameter.CarrierParameter.RandomFrequency;
import vvvfsimulator.vvvf.model.Struct.JerkSettings.Jerk;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.CarrierFrequency;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.RandomModulation;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
public final class Analyze{
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
        if(param.endAmplitude==param.startAmplitude) amplitude=param.startAmplitude;
        else if(param.mode==ValueMode.Linear){
            if(!param.disableRangeLimit){
                current=Math.max(current,param.startFrequency);
                current=Math.min(current,param.endFrequency);
            }
            amplitude=(param.endAmplitude-param.startAmplitude)/(param.endFrequency-param.startFrequency)*
                    (current-param.startFrequency)+param.startAmplitude;
        }
        else if(param.mode==ValueMode.InverseProportional){
            if(!param.disableRangeLimit){
                current=Math.max(current,param.startFrequency);
                current=Math.min(current,param.endFrequency);
            }
            double x=(1.0/param.endAmplitude-1.0/param.startAmplitude)/(param.endFrequency-param.startFrequency)*
                    (current-param.startFrequency)+1.0/param.startAmplitude;
            double c=-param.curveChangeRate;
            double k=param.endAmplitude;
            double l=param.startAmplitude;
            double a=1.0/(1.0/l-1.0/k)*(1.0/(l-c)-1.0/(k-c));
            double b=1.0/(1.0-1.0/l*k)*(1.0/(l-c)-1.0/l*k/(k-c));
            amplitude=1.0/(a*x+b)+c;
        }
        else if(param.mode==ValueMode.Exponential){
            if(!param.disableRangeLimit) current=Math.min(current,param.endFrequency);
            double t=1.0/param.endFrequency*Math.log(param.endAmplitude+1);
            amplitude=Math.exp(t*current)-1;
        }
        else if(param.mode==ValueMode.LinearPolynomial){
            if(!param.disableRangeLimit) current=Math.min(current,param.endFrequency);
            amplitude=Math.pow(current,param.polynomial)/Math.pow(param.endFrequency,param.polynomial)*
                    param.endAmplitude;
        }
        else{
            if(!param.disableRangeLimit) current=Math.min(current,param.endFrequency);
            amplitude=Math.sin(Math.PI*current/(2.0*param.endFrequency))*param.endAmplitude;
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
        double minBaseFrequency=domain.isBraking()?config.minimumFrequency.braking:
                config.minimumFrequency.accelerating;
        if(0<domain.getControlFrequency() && domain.getControlFrequency()<minBaseFrequency && !domain.isFreeRun())
            domain.setControlFrequency(minBaseFrequency);
        domain.setBaseWaveTimeChangeAllowed(!(domain.getBaseWaveFrequency()<minBaseFrequency &&
                domain.getControlFrequency()>0));
        double solvedBaseWaveFrequency=domain.isBaseWaveTimeChangeAllowed()?domain.getBaseWaveFrequency():
                minBaseFrequency;
        List<PulseControlEx> source=new ArrayList<>(domain.isBraking()?config.brakingPattern:
                config.acceleratePattern);
        source.sort(Comparator.comparingDouble((PulseControlEx p)->p.controlFrequencyFrom).reversed());
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
            domain.electricalState=new ElectricalParameter(config.level,solvedBaseWaveFrequency);
            return;
        }
        PulseControlEx solvePattern=source.get(solveIndex);
        Pulse solvePulse=solvePattern.pulseMode;
        CarrierParameter solvedCarrierFrequency=null;
        if(solvePulse.pulseType==Pulse.PulseTypeName.ASYNC){
            var randomData=solvePattern.asyncModulationDataEx.randomData;
            RandomFrequency randomFrequency=new RandomFrequency(
                    randomData.range.mode==RandomModulation.Parameter.ValueMode.Moving?
                    getMovingValue(randomData.range.movingValue,domain.getControlFrequency()):
                    randomData.range.constant,randomData.interval.mode==RandomModulation.Parameter.ValueMode.Moving?
                    getMovingValue(randomData.interval.movingValue,domain.getControlFrequency()):
                    randomData.interval.constant);
            Object baseFrequency;
            if(solvePattern.asyncModulationDataEx.carrierWaveData.mode==CarrierFrequency.ValueMode.Vibrato){
                VibratoValueEx vib=solvePattern.asyncModulationDataEx.carrierWaveData.vibratoData;
                double highest=vib.highest.mode==VibratoValueEx.ParameterEx.ValueMode.Moving?
                        getMovingValue(vib.highest.movingValue,domain.getControlFrequency()):vib.highest.constant;
                double lowest=vib.lowest.mode==VibratoValueEx.ParameterEx.ValueMode.Moving?
                        getMovingValue(vib.lowest.movingValue,domain.getControlFrequency()):vib.lowest.constant;
                double interval=vib.interval.mode==VibratoValueEx.ParameterEx.ValueMode.Moving?
                        getMovingValue(vib.interval.movingValue,domain.getControlFrequency()):vib.interval.constant;
                baseFrequency=new CarrierParameter.VibratoFrequency(highest,lowest,interval);
            }
            else if(solvePattern.asyncModulationDataEx.carrierWaveData.mode==CarrierFrequency.ValueMode.Table){
                List<TableValue.Parameter> table=new ArrayList<>(
                        solvePattern.asyncModulationDataEx.carrierWaveData.carrierFrequencyTable.table);
                table.sort(Comparator.comparingDouble((TableValue.Parameter p)->p.controlFrequencyFrom).reversed());
                TableValue.Parameter target=table.isEmpty()?null:table.getFirst();
                for(TableValue.Parameter p:table){
                    boolean flag1=p.freeRunStuckAtHere && domain.getBaseWaveFrequency()>=p.controlFrequencyFrom &&
                            domain.isFreeRun();
                    boolean flag2=domain.getControlFrequency()>p.controlFrequencyFrom;
                    if(flag1 || flag2){
                        target=p;
                        break;
                    }
                }
                baseFrequency=new CarrierParameter.ConstantFrequency(target==null?0:target.carrierFrequency);
            }
            else if(solvePattern.asyncModulationDataEx.carrierWaveData.mode==CarrierFrequency.ValueMode.Moving){
                baseFrequency=new CarrierParameter.ConstantFrequency(
                        getMovingValue(solvePattern.asyncModulationDataEx.carrierWaveData.movingValue,
                                domain.getControlFrequency()));
            }
            else baseFrequency=new CarrierParameter.ConstantFrequency(
                    solvePattern.asyncModulationDataEx.carrierWaveData.constant);
            solvedCarrierFrequency=new CarrierParameter(randomFrequency,baseFrequency);
        }
        double solvedAmplitude;
        if(domain.isFreeRun()){
            Jerk baseJerk=domain.isBraking()?config.jerkSetting.braking:config.jerkSetting.accelerating;
            Parameter param=(domain.isPowerOff()?
                    solvePattern.amplitude.powerOff:solvePattern.amplitude.powerOn).copy();
            double maxControlFrequency=!domain.isPowerOff()?
                    baseJerk.on.maxControlFrequency:baseJerk.off.maxControlFrequency;
            if(param.endFrequency==-1){
                if(solvePattern.amplitude.defaultValue.disableRangeLimit)
                    param.endFrequency=domain.getBaseWaveFrequency();
                else{
                    param.endFrequency=Math.min(domain.getBaseWaveFrequency(),maxControlFrequency);
                    param.endFrequency=Math.min(param.endFrequency,solvePattern.amplitude.defaultValue.endFrequency);
                }
            }
            if(param.endAmplitude==-1)
                param.endAmplitude=getAmplitude(solvePattern.amplitude.defaultValue,domain.getBaseWaveFrequency());
            if(param.startAmplitude==-1)
                param.startAmplitude=getAmplitude(solvePattern.amplitude.defaultValue,domain.getBaseWaveFrequency());
            solvedAmplitude=getAmplitude(param,domain.getControlFrequency());
        }
        else solvedAmplitude=getAmplitude(solvePattern.amplitude.defaultValue,domain.getControlFrequency());
        Map<PulseDataKey,Double> solvedPulseData=new HashMap<>();
        PulseDataKey[] keys=Config.getAvailablePulseDataKey(solvePulse,config.level);
        for(PulseDataKey key:keys){
            Double value=solvePulse.pulseData!=null && solvePulse.pulseData.containsKey(key)?
                    solvePulse.pulseData.get(key).constant:Config.getPulseDataKeyDefaultConstant(key);
            solvedPulseData.put(key,value);
        }
        if(domain.isPowerOff() && solvedAmplitude==0) domain.setControlFrequency(0);
        domain.electricalState=new ElectricalParameter(false,domain.electricalState.baseWaveAmplitude!=null &&
                        (domain.electricalState.baseWaveAmplitude==0 || domain.getControlFrequency()==0),
                config.level,solvePattern.copy(),solvedCarrierFrequency,
                solvedPulseData,solvedBaseWaveFrequency,solvedAmplitude);
    }
}