package vvvfsimulator.data.vvvf;
public final class Util{
    public static boolean setFreeRunModulationIndexToZero(Struct data){
        for(Struct.PulseControlEx pulse:data.acceleratePattern){
            pulse.amplitude.powerOff.startAmplitude=0;
            pulse.amplitude.powerOff.startFrequency=0;
            pulse.amplitude.powerOn.startAmplitude=0;
            pulse.amplitude.powerOn.startFrequency=0;
        }
        for(Struct.PulseControlEx pulse:data.brakingPattern){
            pulse.amplitude.powerOff.startAmplitude=0;
            pulse.amplitude.powerOff.startFrequency=0;
            pulse.amplitude.powerOn.startAmplitude=0;
            pulse.amplitude.powerOn.startFrequency=0;
        }
        return true;
    }
    public static boolean setFreeRunEndAmplitudeContinuous(Struct data){
        for(Struct.PulseControlEx pulse:data.acceleratePattern){
            pulse.amplitude.powerOff.endAmplitude=-1;
            pulse.amplitude.powerOff.endFrequency=-1;
            pulse.amplitude.powerOn.endAmplitude=-1;
            pulse.amplitude.powerOn.endFrequency=-1;
        }
        for(Struct.PulseControlEx pulse:data.brakingPattern){
            pulse.amplitude.powerOff.endAmplitude=-1;
            pulse.amplitude.powerOff.endFrequency=-1;
            pulse.amplitude.powerOn.endAmplitude=-1;
            pulse.amplitude.powerOn.endFrequency=-1;
        }
        return true;
    }
}