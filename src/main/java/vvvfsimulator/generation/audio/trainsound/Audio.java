package vvvfsimulator.generation.audio.trainsound;
import java.util.List;
import vvvfsimulator.data.trainaudio.Struct;
import vvvfsimulator.data.trainaudio.Struct.HarmonicData;
import vvvfsimulator.vvvf.calculation.Common;
import vvvfsimulator.vvvf.model.Struct.Domain;
public final class Audio{
    public static double calculateHarmonicSounds(Domain control,List<HarmonicData> harmonics){
        double sound=0;
        for(HarmonicData harmonic: harmonics){
            if(harmonic.range.start>control.getBaseWaveFrequency()) continue;
            if(harmonic.range.end>=0 && harmonic.range.end<control.getBaseWaveFrequency()) continue;
            double harmonicFreq=harmonic.harmonic*control.getBaseWaveFrequency();
            if(harmonic.disappear!=-1 && harmonicFreq>harmonic.disappear) continue;
            double sine=Math.sin(control.getBaseWaveTime()*control.getBaseWaveAngleFrequency()*harmonic.harmonic);
            double amp=harmonic.amplitude.startValue+
                    (harmonic.amplitude.endValue-harmonic.amplitude.startValue)/
                    (harmonic.amplitude.end-harmonic.amplitude.start)*
                    (control.getBaseWaveFrequency()-harmonic.amplitude.start);
            amp=Math.min(amp,harmonic.amplitude.maximumValue);
            amp=Math.max(amp,harmonic.amplitude.minimumValue);
            double fade=(harmonic.disappear==-1 || harmonicFreq+100.0<=harmonic.disappear)?
                    1.0:(harmonic.disappear-harmonicFreq)/100.0;
            sound+=sine*amp*fade;
        }
        return sound;
    }
    public static double calculateTrainSound(Domain control,Struct data){
        Common.calculatePhaseState(control,0);
        return calculateTrainSoundFromCurrentState(control,data);
    }
    public static double calculateTrainSoundFromCurrentState(Domain control,Struct data){
        double motorPwm=control.motor.parameter.diffTe*Math.pow(10,data.motorVolumeDb);
        double motor=calculateHarmonicSounds(control,data.harmonicSound);
        double gear=calculateHarmonicSounds(control,data.gearSound);
        return (motorPwm+motor+gear)*Math.pow(10,data.totalVolumeDb);
    }
}