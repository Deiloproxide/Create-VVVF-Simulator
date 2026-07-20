package vvvfsimulator.vvvf.calculation;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.model.Struct.Domain;
import vvvfsimulator.vvvf.model.Struct.PhaseState;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseAlternative;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseTypeName;
import vvvfsimulator.vvvf.modulation.CustomPwm;
public class L3{
    private static void async(Domain domain,double initialPhase,PhaseState out){
        if(domain.electricalState.isNone){
            out.set(0,0,0);
            return;
        }
        domain.getCarrierInstance().processCarrierFrequency(domain.getTime(),domain.electricalState);
        double carrierVal=Common.getCarrierWaveform(domain,domain.getCarrierInstance().getPhase());
        double dipolar=Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.Dipolar);
        carrierVal*=(dipolar!=-1?dipolar:0.5);
        out.set(modulate(Common.getBaseWaveform(domain,0,initialPhase),carrierVal),
                modulate(Common.getBaseWaveform(domain,1,initialPhase),carrierVal),
                modulate(Common.getBaseWaveform(domain,2,initialPhase),carrierVal));
    }
    private static int modulate(double baseWave,double carrier){
        return Common.modulateSignal(baseWave,carrier+0.5)+Common.modulateSignal(baseWave,carrier-0.5);
    }
    private static int sync(Domain domain,double initialPhase,int phase){
        if(domain.electricalState.isNone) return 0;
        double[] baseWaveParameter=domain.getBaseWaveParameterScratch();
        Common.getBaseWaveParameter(domain,phase,initialPhase,baseWaveParameter);
        double x=baseWaveParameter[Common.BASE_WAVE_X];
        double rawX=baseWaveParameter[Common.BASE_WAVE_RAW_X];
        var pulseMode=domain.electricalState.pulsePattern.pulseMode;
        if(pulseMode.pulseCount==1 && pulseMode.alternative==PulseAlternative.Alt1){
            double sineVal=MyMath.Functions.sine(x);
            int d=sineVal>0?1:-1;
            double voltageFix=d*(1-domain.electricalState.baseWaveAmplitude);
            int gate=d*(sineVal-voltageFix)>0?d:0;
            return gate+1;
        }
        if(pulseMode.pulseCount==5 && pulseMode.alternative==PulseAlternative.Alt1){
            double period=x%MyMath.M_2PI;
            int orthant=(int)(period/MyMath.M_PI_2);
            double quarter=period%MyMath.M_PI_2;
            return switch(orthant){
                case 0->getPwmAlt1(domain,quarter);
                case 1->getPwmAlt1(domain,MyMath.M_PI_2-quarter);
                case 2->2-getPwmAlt1(domain,quarter);
                default->2-getPwmAlt1(domain,MyMath.M_PI_2-quarter);
            };
        }
        if(pulseMode.pulseCount==5 && pulseMode.alternative==PulseAlternative.Alt2){
            double sineVal=domain.electricalState.baseWaveAmplitude*MyMath.Functions.sine(rawX);
            double beta=Common.getPulseDataValue(
                    domain.electricalState.pulseData,PulseDataKey.PulseWidth)/180*MyMath.M_PI;
            double cycle=rawX%MyMath.M_2PI;
            int orthant=((int)(cycle/MyMath.M_PI_2))%4;
            double sawVal=switch(orthant){
                case 0->getCarrierAlt2(cycle,beta);
                case 1->getCarrierAlt2(MyMath.M_PI-cycle,beta);
                case 2->-getCarrierAlt2(cycle-MyMath.M_PI,beta);
                default->-getCarrierAlt2(MyMath.M_2PI-cycle,beta);
            };
            return orthant<=1?Common.modulateSignal(sineVal,sawVal)+1:-Common.modulateSignal(sawVal,sineVal)+1;
        }
        domain.getCarrierInstance().angleFrequency=domain.electricalState.getBaseWaveAngleFrequency();
        domain.getCarrierInstance().time=domain.getBaseWaveTime();
        double sineVal=Common.getBaseWaveform(domain,phase,initialPhase);
        double carrierVal=Common.getCarrierWaveform(
                domain,domain.electricalState.pulsePattern.pulseMode.pulseCount*rawX);
        double dipolar=Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.Dipolar);
        carrierVal*=(dipolar!=-1?dipolar:0.5);
        return Common.modulateSignal(sineVal,carrierVal+0.5)+
                Common.modulateSignal(sineVal,carrierVal-0.5);
    }
    private static int getPwmAlt1(Domain domain,double t){
        double a=MyMath.M_PI_2-domain.electricalState.baseWaveAmplitude;
        double b=Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.PulseWidth)/180*MyMath.M_PI;
        if(t<a) return 1;
        if(t<a+b) return 2;
        if(t<a+2*b) return 1;
        return 2;
    }
    private static double getCarrierAlt2(double x,double beta){
        if(0<=x && x<beta) return -(1/beta)*x+1;
        if(beta<=x && x<MyMath.M_PI_2) return -1/(MyMath.M_PI_2-beta)*(x-MyMath.M_PI_2);
        return 0;
    }
    private static void sync(Domain domain,double initialPhase,PhaseState out){
        out.set(sync(domain,initialPhase,0),
                sync(domain,initialPhase,1),
                sync(domain,initialPhase,2));
    }
    private static void fromCustomPwm(Domain domain,double initialPhase,PhaseState out){
        if(domain.electricalState.isNone){
            out.set(0,0,0);
            return;
        }
        CustomPwm preset=CustomPwm.CustomPwmPresets.getCustomPwm(
                domain.electricalState.pwmLevel,
                domain.electricalState.pulsePattern.pulseMode.pulseType,
                domain.electricalState.pulsePattern.pulseMode.pulseCount,
                domain.electricalState.pulsePattern.pulseMode.alternative);
        if(preset==null){
            out.set(0,0,0);
            return;
        }
        double[] baseWaveParameter=domain.getBaseWaveParameterScratch();
        Common.getBaseWaveParameter(domain,0,initialPhase,baseWaveParameter);
        int u=preset.getPwm(domain.electricalState.baseWaveAmplitude,
                baseWaveParameter[Common.BASE_WAVE_X]);
        Common.getBaseWaveParameter(domain,1,initialPhase,baseWaveParameter);
        int v=preset.getPwm(domain.electricalState.baseWaveAmplitude,
                baseWaveParameter[Common.BASE_WAVE_X]);
        Common.getBaseWaveParameter(domain,2,initialPhase,baseWaveParameter);
        int w=preset.getPwm(domain.electricalState.baseWaveAmplitude,
                baseWaveParameter[Common.BASE_WAVE_X]);
        out.set(u,v,w);
    }
    public static Common.PhaseStateCalculator getCalculator(PulseTypeName pulseType){
        return switch(pulseType){
            case ASYNC -> L3::async;
            case SYNC -> L3::sync;
            default -> L3::fromCustomPwm;
        };
    }
}