package vvvfsimulator.vvvf.calculation;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.modulation.CustomPwm;
import vvvfsimulator.vvvf.modulation.DeltaSigma;
import vvvfsimulator.vvvf.model.Struct.Domain;
import vvvfsimulator.vvvf.model.Struct.PhaseState;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseAlternative;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseTypeName;
public class L2{
    private static final int[] HO_KEYS_5={9,2,13,2,17,2,21,2,25,2,29,2,33,2,37,2};
    private static final int[] HO_KEYS_7={15,4,15,3,7,1,11,2,19,4,23,4,27,4,31,4,35,4,39,4};
    private static final int[] HO_KEYS_9={21,6,13,3,17,4,25,6,29,6,33,6,37,6};
    private static final int[] HO_KEYS_11={27,8,19,5,23,6,31,8,35,8,39,8};
    private static final int[] HO_KEYS_13={25,7,29,8,33,10,37,10};
    private static final int[] HO_KEYS_15={31,9,35,10,39,12};
    private static final int[] HO_KEYS_17={37,11};
    private static final int[] HO_KEYS_DEFAULT={0};
    private static void async(Domain domain,double initialPhase,PhaseState out){
        domain.getCarrierInstance().processCarrierFrequency(domain.getTime(),domain.electricalState);
        double carrierVal=Common.getCarrierWaveform(domain,domain.getCarrierInstance().getPhase());
        carrierVal=MyMath.Functions.triangle(carrierVal*MyMath.M_PI_2*
                Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.CarrierFolding));
        out.set(Common.modulateSignal(
                Common.getBaseWaveform(domain,0,initialPhase),carrierVal)*2,
                Common.modulateSignal(Common.getBaseWaveform(domain,1,initialPhase),carrierVal)*2,
                Common.modulateSignal(Common.getBaseWaveform(domain,2,initialPhase),carrierVal)*2);
    }
    private static int sync(Domain domain,double initialPhase,int phase){
        if(domain.electricalState.isNone) return 0;
        double[] baseWaveParameter=domain.getBaseWaveParameterScratch();
        Common.getBaseWaveParameter(domain,phase,initialPhase,baseWaveParameter);
        double x=baseWaveParameter[Common.BASE_WAVE_X];
        double rawX=baseWaveParameter[Common.BASE_WAVE_RAW_X];
        var pulseMode=domain.electricalState.pulsePattern.pulseMode;
        domain.getCarrierInstance().angleFrequency=domain.electricalState.getBaseWaveAngleFrequency();
        domain.getCarrierInstance().time=domain.getBaseWaveTime();
        if(pulseMode.pulseCount==1 &&
                (pulseMode.alternative==PulseAlternative.Alt1 || pulseMode.alternative==PulseAlternative.Alt2)){
            int sign=pulseMode.alternative==PulseAlternative.Alt1?1:-1;
            double ampAbs=Math.abs(domain.electricalState.baseWaveAmplitude);
            int ampSign=domain.electricalState.baseWaveAmplitude<0?-1:1;
            double sineVal=-MyMath.Functions.triangle(x)+
                    sign*(1-MyMath.Functions.arcSine(Math.min(Math.max(ampAbs*MyMath.M_PI_4,0),1))*MyMath.M_2_PI);
            return sineVal>0?-ampSign+1:ampSign+1;
        }
        if(pulseMode.pulseCount==3 && pulseMode.alternative==PulseAlternative.Alt1){
            double sineVal=MyMath.Functions.sine(rawX);
            double sawVal=-MyMath.Functions.triangle(rawX-Common.getPulseDataValue(
                    domain.electricalState.pulseData,PulseDataKey.Phase)/180.0*MyMath.M_PI);
            double pwm=(sineVal>0?1:-1)*(domain.electricalState.baseWaveAmplitude*2.0/3.0+1.0/3.0);
            double negate=sawVal>0?sawVal-1:sawVal+1;
            return Common.modulateSignal(pwm,negate)*2;
        }
        if(pulseMode.pulseCount==3 &&
                (pulseMode.alternative==PulseAlternative.Alt2 ||
                        pulseMode.alternative==PulseAlternative.Alt3)){
            double sineVal=domain.electricalState.baseWaveAmplitude*
                    MyMath.Functions.square(MyMath.Functions.sine(rawX));
            double s=1-Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.PulseWidth);
            double cycle=rawX%MyMath.M_2PI;
            int orthant=(int)(cycle/MyMath.M_PI_2)%4;
            double sawVal=switch(orthant){
                case 0->getCarrierAlt3(cycle,s);
                case 1->getCarrierAlt3(MyMath.M_PI-cycle,s);
                case 2->-getCarrierAlt3(cycle-MyMath.M_PI,s);
                default->-getCarrierAlt3(MyMath.M_2PI-cycle,s);
            };
            if(pulseMode.alternative==PulseAlternative.Alt3) sawVal=-sawVal;
            return Common.modulateSignal(sineVal,sawVal)*2;
        }
        if((pulseMode.pulseCount==5 || pulseMode.pulseCount==9 ||
                pulseMode.pulseCount==13 || pulseMode.pulseCount==17) &&
                pulseMode.alternative==PulseAlternative.Alt1){
            double sineVal=Common.getBaseWaveform(domain,phase,initialPhase);
            double sawValue=-MyMath.Functions.triangle(27*rawX);
            double fixedX=(((int)(rawX/MyMath.M_PI_2))%2==1)?MyMath.M_PI_2-rawX%MyMath.M_PI_2:rawX%MyMath.M_PI_2;
            domain.getCarrierInstance().angleFrequency=domain.electricalState.getBaseWaveAngleFrequency();
            domain.getCarrierInstance().time=domain.getBaseWaveTime();
            return (fixedX<MyMath.M_PI*pulseMode.pulseCount/54.0)?
                    Common.modulateSignal(sineVal,sawValue)*2:(((int)(rawX/MyMath.M_PI_2))%4>1?0:2);
        }
        if(pulseMode.pulseCount==11 && pulseMode.alternative==PulseAlternative.Alt1){
            double amplitude=domain.electricalState.baseWaveAmplitude;
            CustomPwm.SwitchEntry[] alpha=domain.getSwitchEntryScratch();
            getSwitchEntries(amplitude,alpha);
            if(amplitude>=0.9927) setSwitchEntry(alpha[0],0.0,(byte)2);
            if(amplitude>=0.9203069589){
                setSwitchEntry(alpha[1],0.417331,(byte)0);
                setSwitchEntry(alpha[2],0.417331,(byte)2);
                setSwitchEntry(alpha[3],1.23442104526+0.278769982056*(amplitude-0.9203069589),(byte)0);
                setSwitchEntry(alpha[4],1.32231416347-0.824126360283*(amplitude-0.9203069589),(byte)2);
            }
            return CustomPwm.getPwm(alpha,x,(byte)0);
        }
        if((pulseMode.pulseCount==6 || pulseMode.pulseCount==8) && pulseMode.alternative==PulseAlternative.Alt1){
            int c=pulseMode.pulseCount==6?6:9;
            double sawVal=-MyMath.Functions.triangle(c*rawX+MyMath.M_PI_2);
            int orthant=(int)((rawX%MyMath.M_2PI)/MyMath.M_PI_2);
            double fixX=orthant%2==1?MyMath.M_PI_2-(rawX%MyMath.M_PI_2):(rawX%MyMath.M_PI_2);
            double sig=orthant>1?1:-1;
            if(fixX>Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.PulseWidth))
                sig=orthant>1?-1:1;
            sig*=domain.electricalState.baseWaveAmplitude;
            return Common.modulateSignal(sig,sawVal)*2;
        }
        if(pulseMode.alternative==PulseAlternative.CP){
            double sineVal=Common.getBaseWaveform(domain,phase,initialPhase);
            double sawVal=-MyMath.Functions.triangle(
                    3.0*(pulseMode.pulseCount-1)/2.0*rawX)*
                    MyMath.Functions.square(MyMath.Functions.sine(
                            3.0*(pulseMode.pulseCount-1)/2.0*rawX))*
                    MyMath.Functions.square(MyMath.Functions.sine(3.0*rawX));
            sawVal=MyMath.Functions.triangle(sawVal*MyMath.M_PI_2*
                    Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.CarrierFolding));
            return Common.modulateSignal(sineVal,sawVal)*2;
        }
        if(pulseMode.alternative==PulseAlternative.ShiftedCP){
            double sineVal=Common.getBaseWaveform(domain,phase,initialPhase);
            int n=(pulseMode.pulseCount+1)/4;
            double p=MyMath.Functions.square(MyMath.Functions.sine(3*n*x))*
                    MyMath.Functions.square(MyMath.Functions.sine(3*x));
            double sawVal=MyMath.Functions.triangle(3*n*x)*p;
            sawVal=MyMath.Functions.triangle(sawVal*MyMath.M_PI_2*
                    Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.CarrierFolding));
            return Common.modulateSignal(sineVal,sawVal)*2;
        }
        if(pulseMode.alternative==PulseAlternative.Square){
            int pulseCount=pulseMode.pulseCount%2==0?
                    (int)(pulseMode.pulseCount*1.5):
                    (int)((pulseMode.pulseCount-1)*1.5);
            double carrierPhase=pulseMode.pulseCount%2==0?MyMath.M_PI_2:0;
            double carrierVal=-MyMath.Functions.triangle(pulseCount*rawX+carrierPhase);
            carrierVal=MyMath.Functions.triangle(carrierVal*MyMath.M_PI_2*
                    Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.CarrierFolding));
            return Common.modulateSignal(MyMath.Functions.sine(rawX)>0?
                    domain.electricalState.baseWaveAmplitude:
                    -domain.electricalState.baseWaveAmplitude,carrierVal)*2;
        }
        double sineVal=Common.getBaseWaveform(domain,phase,initialPhase);
        double carrierVal=Common.getCarrierWaveform(domain,pulseMode.pulseCount*rawX);
        carrierVal=MyMath.Functions.triangle(carrierVal*MyMath.M_PI_2*
                Common.getPulseDataValue(domain.electricalState.pulseData,PulseDataKey.CarrierFolding));
        return Common.modulateSignal(sineVal,carrierVal)*2;
    }
    private static double getCarrierAlt3(double x,double s){
        if(0<=x && x<s*MyMath.M_PI_6){
            return -6/(s*MyMath.M_PI)*x;
        }
        if(s*MyMath.M_PI_6<=x && x<s*MyMath.M_PI_2){
            return 6/(s*MyMath.M_PI)*(x-s*MyMath.M_PI_6)-1;
        }
        if(s*MyMath.M_PI_2<=x && x<MyMath.M_PI_2){
            return 3/((s-1)*MyMath.M_PI)*(x-MyMath.M_PI_2)-0.5;
        }
        return 0;
    }
    private static void getSwitchEntries(double amplitude,CustomPwm.SwitchEntry[] out){
        double sqrt5=Math.sqrt(5.0);
        double sqrt3=Math.sqrt(3.0);
        setSwitchEntry(out[0],MyMath.M_PI/15.0-(1.0+sqrt5)/(10.0*sqrt3)*amplitude-
                2.0*MyMath.Functions.sine(MyMath.M_PI/30.0)/(5.0*sqrt3)*amplitude,(byte)2);
        setSwitchEntry(out[1],MyMath.M_PI/15.0+(sqrt5-1.0)/(10.0*sqrt3)*amplitude+
                2.0*MyMath.Functions.sine(MyMath.M_PI*7.0/30.0)/(5.0*sqrt3)*amplitude,(byte)0);
        setSwitchEntry(out[2],MyMath.M_PI/6.0-1.0/(5.0*sqrt3)*amplitude,(byte)2);
        setSwitchEntry(out[3],MyMath.M_PI*2.0/5.0-2.0*MyMath.Functions.sine(
                MyMath.M_PI/30.0)/(5.0*sqrt3)*amplitude,(byte)0);
        setSwitchEntry(out[4],MyMath.M_PI*2.0/5.0+(sqrt5-1.0)/(10.0*sqrt3)*amplitude,(byte)2);
    }
    private static void setSwitchEntry(CustomPwm.SwitchEntry entry,double switchAngle,byte output){
        entry.switchAngle=switchAngle;
        entry.output=output;
    }
    private static void sync(Domain domain,double initialPhase,PhaseState out){
        out.set(sync(domain,initialPhase,0),
                sync(domain,initialPhase,1),sync(domain,initialPhase,2));
    }
    private static int ho(Domain domain,double initialPhase,int phase){
        if(domain.electricalState.isNone) return 0;
        double[] baseWaveParameter=domain.getBaseWaveParameterScratch();
        Common.getBaseWaveParameter(domain,phase,initialPhase,baseWaveParameter);
        double sineX=baseWaveParameter[Common.BASE_WAVE_X];
        int[] keys=getKeys(domain);
        int index;
        var pulseMode=domain.electricalState.pulsePattern.pulseMode;
        if(pulseMode.alternative==PulseAlternative.Default ||
                pulseMode.alternative.ordinal()-PulseAlternative.Alt1.ordinal()+1>=keys.length/2)
            index=0;
        else
            index=pulseMode.alternative.ordinal()-PulseAlternative.Alt1.ordinal()+1;
        int carrier=keys[2*index];
        int width=keys[2*index+1];
        return getHo(sineX,domain.electricalState.baseWaveAmplitude,carrier,width)*2;
    }
    private static int[] getKeys(Domain domain){
        return switch(domain.electricalState.pulsePattern.pulseMode.pulseCount){
            case 5->HO_KEYS_5;
            case 7->HO_KEYS_7;
            case 9->HO_KEYS_9;
            case 11->HO_KEYS_11;
            case 13->HO_KEYS_13;
            case 15->HO_KEYS_15;
            case 17->HO_KEYS_17;
            default->HO_KEYS_DEFAULT;
        };
    }
    private static int getHo(double x,double amplitude,int carrier,int width){
        int totalSteps=carrier*2;
        double fixedX=x%MyMath.M_PI/(MyMath.M_PI/totalSteps);
        double sawValue=MyMath.Functions.triangle(carrier*x);
        double modulated;
        if(fixedX>totalSteps-1) modulated=-1;
        else if(fixedX>totalSteps/2.0+width) modulated=1;
        else if(fixedX>totalSteps/2.0-width) modulated=2*amplitude-1;
        else if(fixedX>1) modulated=1;
        else modulated=-1;
        if(x%MyMath.M_2PI>MyMath.M_PI) modulated=-modulated;
        return Common.modulateSignal(modulated,sawValue);
    }
    private static void ho(Domain domain,double initialPhase,PhaseState out){
        out.set(ho(domain,initialPhase,0),
                ho(domain,initialPhase,1),ho(domain,initialPhase,2));
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
    private static int deltaSigma(Domain domain,double initialPhase,int phase){
        if(domain.electricalState.isNone) return 0;
        DeltaSigma deltaSigma=domain.getDeltaSigmaInstance(phase);
        deltaSigma.resetIfLastTime(domain.getLastTime());
        deltaSigma.feedbackInterval=1.0/Common.getPulseDataValue(
                domain.electricalState.pulseData,PulseDataKey.UpdateFrequency);
        return deltaSigma.process(Common.getBaseWaveform(domain,phase,initialPhase),domain.getTime())*2;
    }
    private static void deltaSigma(Domain domain,double initialPhase,PhaseState out){
        out.set(deltaSigma(domain,initialPhase,0),
                deltaSigma(domain,initialPhase,1),deltaSigma(domain,initialPhase,2));
    }
    public static Common.PhaseStateCalculator getCalculator(PulseTypeName pulseType){
        return switch(pulseType){
            case ASYNC->L2::async;
            case SYNC->L2::sync;
            case HO->L2::ho;
            case DELTA_SIGMA->L2::deltaSigma;
            default->L2::fromCustomPwm;
        };
    }
}