package vvvfsimulator.generation.audio.trainsound;
import vvvfsimulator.data.vvvf.Analyze;
import vvvfsimulator.generation.audio.RealTime.TrainSoundParameter;
public class RealTime{
    public static void generate(TrainSoundParameter parameter){
        while(!parameter.quit){
            int flag=vvvfsimulator.generation.audio.RealTime.realTimeFrequencyControl(
                    parameter.control,parameter,1.0/44100.0);
            if(flag!=-1) break;
            Analyze.calculate(parameter.control,parameter.vvvfSoundData);
            parameter.control.addTimeAll(1.0/44100.0);
            Audio.calculateTrainSound(parameter.control,parameter.trainSoundData);
        }
    }
}