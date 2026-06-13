package vvvfsimulator.generation;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.calculation.Common;
import vvvfsimulator.vvvf.model.Struct;
public final class GenerateBasic{
    public static final class WaveForm{
        public static Struct.PhaseState[] getUVWCycle(Struct.Domain control,double initialPhase,
                                                      int division,boolean precise){
            double f=control.electricalState.baseWaveFrequency;
            double k=(f>0.01 && f<1)?1.0/f:1;
            int count=precise?(int)Math.round(division*k):division;
            double invDeltaT=count*f;
            return getUVW(control,initialPhase,invDeltaT,count);
        }
        public static Struct.PhaseState[] getUVW(Struct.Domain control,double initialPhase,
                                                 double invDeltaT,int count){
            Struct.PhaseState[] out=new Struct.PhaseState[count+1];
            control.resetTimeAll();
            for(int i=0;i<=count;i++){
                control.setTimeAll(Double.isNaN(invDeltaT)?0:i/invDeltaT);
                out[i]=Common.calculatePhaseState(control,initialPhase);
            }
            return out;
        }
    }
    public static final class Fourier{
        public static final double VOLTAGE_CONVERT_FACTOR=1.102657791;
        public static double getFourierFast(Struct.PhaseState[] uvw,int n){
            double integral=0;
            int ft=0;
            double time=0;
            for(int i=0;i<uvw.length;i++){
                int iFt=uvw[i].u-uvw[i].v;
                if(i==0){
                    ft=iFt;
                    continue;
                }
                if(ft==iFt) continue;
                double iTime=MyMath.M_2PI*i/(uvw.length-1);
                integral+=(-Math.cos(n*iTime)+Math.cos(n*time))*ft/n;
                time=iTime;
                ft=iFt;
            }
            return integral/MyMath.M_2PI;
        }
        public static double getVoltageRate(Struct.Domain control,boolean precise,boolean fixSign){
            Struct.PhaseState[] pwm=WaveForm.getUVWCycle(control,MyMath.M_PI_6,120000,precise);
            double result=getFourierFast(pwm,1)/VOLTAGE_CONVERT_FACTOR;
            return fixSign?Math.abs(result):result;
        }
    }
}