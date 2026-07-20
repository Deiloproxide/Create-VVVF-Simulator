package vvvfsimulator.generation.audio.vvvfsound;
import vvvfsimulator.data.vvvf.Analyze;
import vvvfsimulator.generation.audio.RealTime.VvvfSoundParameter;
import vvvfsimulator.vvvf.calculation.Common;
import vvvfsimulator.vvvf.model.Struct.PhaseState;
public class RealTime{
    public static void calculate(VvvfSoundParameter parameter){
        while(!parameter.quit){
            int flag=vvvfsimulator.generation.audio.RealTime.realTimeFrequencyControl(
                    parameter.control,parameter,1.0/44100.0);
            if(flag!=-1) break;
            Analyze.calculate(parameter.control,parameter.vvvfSoundData);
            parameter.control.addTimeAll(1.0/44100.0);
            PhaseState value=Common.calculatePhaseState(parameter.control,0);
            double sound=switch(parameter.outputMode){
                case Line->value.u-value.v;
                case Phase->value.u-1;
                case PhaseCurrent->value.u-value.v*0.5-value.w*0.5;
            };
            @SuppressWarnings("unused")
            double sample=sound*0.5;
        }
    }
}