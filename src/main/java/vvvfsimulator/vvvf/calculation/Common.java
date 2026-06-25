package vvvfsimulator.vvvf.calculation;
import java.util.Map;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.modulation.SVM;
import vvvfsimulator.vvvf.model.Config;
import vvvfsimulator.vvvf.model.Struct.Domain;
import vvvfsimulator.vvvf.model.Struct.PhaseState;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.BaseWaveType;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.CarrierWaveConfiguration;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.DiscreteTimeConfiguration.DiscreteTimeMode;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseHarmonic;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseTypeName;
public final class Common{
    public interface PhaseStateCalculator{
        PhaseState calculate(Domain control,double initialPhase);
    }
    public record BaseWaveParameter(double x,double rawX){
    }
    public static int modulateSignal(double signal,double carrier){
        return signal>carrier?1:0;
    }
    public static double getPulseDataValue(Map<PulseDataKey,Double> pulseData,PulseDataKey key){
        if(pulseData==null) return Config.getPulseDataKeyDefaultConstant(key);
        return pulseData.getOrDefault(key,Config.getPulseDataKeyDefaultConstant(key));
    }
    public static double discreteTimeLine(double x,int level,DiscreteTimeMode mode){
        double seed=(x%MyMath.M_2PI)*level/MyMath.M_2PI;
        double time=switch(mode){
            case Left->Math.ceil(seed);
            case Middle->Math.rint(seed);
            case Right->Math.floor(seed);
        };
        return time*MyMath.M_2PI/level;
    }
    public static BaseWaveParameter getBaseWaveParameter(Domain control,int phase,double initialPhase){
        if(control.electricalState.isNone) return new BaseWaveParameter(0,0);
        double sineTime=control.getBaseWaveTime();
        double rawX=control.electricalState.getBaseWaveAngleFrequency()*sineTime+MyMath.M_2PI_3*phase+initialPhase;
        double sineX;
        if(control.electricalState.pulsePattern.pulseMode.discreteTime.enabled){
            sineX=discreteTimeLine(rawX,
                    control.electricalState.pulsePattern.pulseMode.discreteTime.steps,
                    control.electricalState.pulsePattern.pulseMode.discreteTime.mode);
        }
        else sineX=rawX;
        return new BaseWaveParameter(sineX,rawX);
    }
    public static double getBaseWaveform(Domain control,int phase,double initialPhase){
        if(control.electricalState.isNone || control.electricalState.baseWaveAmplitude==null) return 0;
        double amp=control.electricalState.baseWaveAmplitude;
        BaseWaveType baseWaveType=control.electricalState.pulsePattern.pulseMode.baseWave;
        if(baseWaveType==BaseWaveType.SV){
            SVM.Vabc vabc=new SVM.Vabc();
            vabc.u=amp*MyMath.Functions.sine(getBaseWaveParameter(control,0,initialPhase).x());
            vabc.v=amp*MyMath.Functions.sine(getBaseWaveParameter(control,1,initialPhase).x());
            vabc.w=amp*MyMath.Functions.sine(getBaseWaveParameter(control,2,initialPhase).x());
            SVM.Valbe valbe=vabc.clark();
            int sector=valbe.estimateSector();
            SVM.FunctionTime ft=valbe.getFunctionTime(sector);
            SVM.Vabc vsv=ft.getVabc(sector);
            double ret=switch(phase){
                case 0->vsv.u;
                case 1->vsv.v;
                default->vsv.w;
            };
            return ret*2-1;
        }
        BaseWaveParameter p=getBaseWaveParameter(control,phase,initialPhase);
        double x=p.x();
        if(baseWaveType==BaseWaveType.DPWM30 || baseWaveType==BaseWaveType.DPWM60C ||
                baseWaveType==BaseWaveType.DPWM60P || baseWaveType==BaseWaveType.DPWM60N ||
                baseWaveType==BaseWaveType.DPWM120P || baseWaveType==BaseWaveType.DPWM120N){
            int sector6=(int)((x%MyMath.M_2PI)/MyMath.M_PI_6);
            int sector3=(int)((x%MyMath.M_2PI)/MyMath.M_PI_3);
            return switch(baseWaveType){
                case DPWM30->switch(sector6){
                    case 0,9->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                    case 1,4->1;
                    case 2,11->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    case 3,6->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                    case 5,8->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    default->-1;
                };
                case DPWM60C->switch(sector3){
                    case 0->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    case 1->1;
                    case 2->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                    case 3->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    case 4->-1;
                    default->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                };
                case DPWM60P->switch(sector6){
                    case 1,2->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    case 3,4->1;
                    case 5,6->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                    case 7,8->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    case 9,10->-1;
                    default->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                };
                case DPWM60N->switch(sector6){
                    case 1,2->1;
                    case 3,4->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                    case 5,6->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    case 7,8->-1;
                    case 9,10->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                    default->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                };
                case DPWM120P->switch(sector6){
                    case 1,2,3,4->1;
                    case 5,6,7,8->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                    default->amp*MyMath.Functions.sine(x)+(1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                };
                case DPWM120N->switch(sector6){
                    case 3,4,5,6->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase+MyMath.M_2PI_3).x()));
                    case 7,8,9,10->-1;
                    default->amp*MyMath.Functions.sine(x)+(-1-amp*MyMath.Functions.sine(
                            getBaseWaveParameter(control,phase,initialPhase-MyMath.M_2PI_3).x()));
                };
                default->0;
            };
        }
        double baseWave=getBaseWave(x,amp,baseWaveType);
        double harmonicWave=0;
        for(PulseHarmonic harmonicData:control.electricalState.pulsePattern.pulseMode.pulseHarmonics){
            double harmonicX=harmonicData.isHarmonicProportional?
                    harmonicData.harmonic*(x+harmonicData.initialPhase):
                    MyMath.M_2PI*harmonicData.harmonic*(control.getTime()+initialPhase);
            double wave=switch(harmonicData.type){
                case Sine->MyMath.Functions.sine(harmonicX);
                case Saw->MyMath.Functions.triangle(harmonicX);
                case Square->MyMath.Functions.square(harmonicX);
            };
            harmonicWave+=wave*harmonicData.amplitude*(harmonicData.isAmplitudeProportional?amp:1);
        }
        double wave=baseWave+harmonicWave;
        return Math.min(Math.max(wave,-1),1);
    }
    private static double getBaseWave(double x,double amp,BaseWaveType baseWaveType){
        double getModifiedSine=Math.round(MyMath.Functions.sine(x)*2.0)/2.0;
        double y=MyMath.Functions.triangle(x)*MyMath.M_PI_2;
        if(Math.abs(y)>0.5) y=y>0?1:-1;
        return amp*switch(baseWaveType){
            case Sine->MyMath.Functions.sine(x);
            case Saw->MyMath.Functions.triangle(x);
            case Square->MyMath.Functions.square(x);
            case ModifiedSine1->Math.round(MyMath.Functions.sine(x));
            case ModifiedSine2->getModifiedSine;
            case ModifiedSaw1->y;
            default -> 0;
        };
    }
    public static double getCarrierWaveform(Domain control,double time){
        if(control.electricalState.isNone) return 0;
        CarrierWaveConfiguration cfg=control.electricalState.pulsePattern.pulseMode.carrierWave;
        return switch(cfg.type){
            case Triangle->switch(cfg.option){
                case RaiseStart->MyMath.Functions.triangle(time);
                case FallStart->-MyMath.Functions.triangle(time);
                case TopStart->MyMath.Functions.triangle(time+MyMath.M_PI_2);
                case BottomStart->-MyMath.Functions.triangle(time+MyMath.M_PI_2);
            };
            case Saw->switch(cfg.option){
                case RaiseStart->MyMath.Functions.saw(time);
                case FallStart->-MyMath.Functions.saw(time);
                case TopStart->-MyMath.Functions.saw(time+MyMath.M_PI_2);
                case BottomStart->MyMath.Functions.saw(time+MyMath.M_PI_2);
            };
            case Sine->switch(cfg.option){
                case RaiseStart->MyMath.Functions.sine(time);
                case FallStart->-MyMath.Functions.sine(time);
                case TopStart->MyMath.Functions.sine(time+MyMath.M_PI_2);
                case BottomStart->-MyMath.Functions.sine(time+MyMath.M_PI_2);
            };
        };
    }
    public static PhaseStateCalculator getCalculator(int pwmLevel,PulseTypeName pulseType){
        return pwmLevel==2?L2.getCalculator(pulseType):L3.getCalculator(pulseType);
    }
    public static PhaseState calculatePhaseState(Domain control,double initialPhase){
        PhaseState state=PhaseState.zero();
        if(!control.electricalState.isNone && !control.electricalState.isZeroOutput)
            state=getCalculator(control.electricalState.pwmLevel,
                    control.electricalState.pulsePattern.pulseMode.pulseType).calculate(control,initialPhase);
        control.motor.process(control.getDeltaTime(),control.electricalState.getBaseWaveAngleFrequency(),state);
        return state;
    }
}