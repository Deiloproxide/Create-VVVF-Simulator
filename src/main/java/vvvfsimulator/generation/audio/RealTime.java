package vvvfsimulator.generation.audio;
import vvvfsimulator.data.trainaudio.Struct;
import vvvfsimulator.vvvf.model.Struct.Domain;
public class RealTime{
    public static class Parameter{
        public double frequencyChangeRate=0;
        public boolean isBraking=false;
        public boolean quit=false;
        public boolean isFreeRunning=false;
        public Domain control;
        public vvvfsimulator.data.vvvf.Struct vvvfSoundData;
        public Struct trainSoundData;
        public Parameter(vvvfsimulator.data.vvvf.Struct vvvfSound,Struct trainSound){
            this.control=new Domain(trainSound.motorSpec);
            this.vvvfSoundData=vvvfSound;
            this.trainSoundData=trainSound;
        }
    }
    public static class VvvfSoundParameter extends Parameter{
        public Mode outputMode=Mode.Line;
        public VvvfSoundParameter(vvvfsimulator.data.vvvf.Struct vvvfSound,Struct trainSound){
            super(vvvfSound,trainSound);
        }
        public enum Mode{
            Line,Phase,PhaseCurrent
        }
    }
    public static class TrainSoundParameter extends Parameter{
        public TrainSoundParameter(vvvfsimulator.data.vvvf.Struct vvvfSound,Struct trainSound){
            super(vvvfSound,trainSound);
        }
    }
    public static int realTimeFrequencyControl(Domain control,Parameter param,double dt){
        control.setBraking(param.isBraking);
        control.setPowerOff(param.isFreeRunning);
        double newAngleFreq=control.getBaseWaveAngleFrequency();
        newAngleFreq+=param.frequencyChangeRate*dt;
        if(newAngleFreq<0) newAngleFreq=0;
        if(!control.isFreeRun()){
            if(control.isBaseWaveTimeChangeAllowed() && newAngleFreq!=0)
                control.multiplyBaseWaveTime(control.getBaseWaveAngleFrequency()/newAngleFreq);
            control.setControlFrequency(control.getBaseWaveFrequency());
            control.setBaseWaveAngleFrequency(newAngleFreq);
        }
        if(param.quit) return 0;
        control.processControlParameter(dt,param.vvvfSoundData.jerkSetting);
        return -1;
    }
}