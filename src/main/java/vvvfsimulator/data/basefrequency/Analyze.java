package vvvfsimulator.data.basefrequency;
import vvvfsimulator.data.basefrequency.StructCompiled.Point;
import vvvfsimulator.data.vvvf.Struct;
import vvvfsimulator.vvvf.model.Struct.Domain;
public final class Analyze{
    private static int getPointAtNum(double time,StructCompiled compiled){
        if(compiled.points.isEmpty()) return -1;
        if(time<compiled.points.getFirst().startTime || compiled.points.getLast().endTime<time)
            return -1;
        int left=0;
        int right=compiled.points.size()-1;
        int pos=(right-left)/2+left;
        while(true){
            Point point=compiled.points.get(pos);
            if(point.startTime<=time && time<point.endTime) break;
            if(point.startTime<time) left=pos+1;
            else right=pos-1;
            pos=(right-left)/2+left;
        }
        return pos;
    }
    private static Point getPointAtData(double time,StructCompiled compiled){
        return compiled.points.get(getPointAtNum(time,compiled));
    }
    private static double getFreqAt(double time,double initial,StructCompiled compiled){
        Point p=getPointAtData(time,compiled);
        double aFrequency=(p.endFrequency-p.startFrequency)/(p.endTime-p.startTime);
        return aFrequency*(time-p.startTime)+p.startFrequency+initial;
    }
    public static boolean checkForFreqChange(Domain control,StructCompiled data,Struct soundData,double deltaTime){
        double forceOnFrequency=-1;
        double currentTime=control.getTime();
        int index=getPointAtNum(currentTime,data);
        if(index<0) return false;
        Point target=data.points.get(index);
        Point next=index+1<data.points.size()?data.points.get(index+1):null;
        Point prev=index-1>=0?data.points.get(index-1):null;
        boolean braking=!target.isAccel;
        boolean isPowerOn=target.isPowerOn;
        if(!isPowerOn && prev!=null) braking=!prev.isAccel;
        if(next!=null && control.isFreeRun() && next.isPowerOn){
            double powerOnFrequency=getFreqAt(target.endTime,0,data);
            double freqPerSec=!next.isAccel?soundData.jerkSetting.braking.on.frequencyChangeRate:
                    soundData.jerkSetting.accelerating.on.frequencyChangeRate;
            double freqGoto=!next.isAccel?soundData.jerkSetting.braking.on.maxControlFrequency:
                    soundData.jerkSetting.accelerating.on.maxControlFrequency;
            double targetFrequency=Math.min(powerOnFrequency,freqGoto);
            double requireTime=targetFrequency/freqPerSec;
            if(target.endTime-requireTime<control.getTime()){
                isPowerOn=true;
                braking=!next.isAccel;
                forceOnFrequency=powerOnFrequency;
            }
        }
        double newSineFrequency=getFreqAt(currentTime,0,data);
        if(newSineFrequency<0) newSineFrequency=0;
        control.setBraking(braking);
        control.setPowerOff(!isPowerOn);
        double amp=newSineFrequency==0?0:control.getBaseWaveFrequency()/newSineFrequency;
        control.setBaseWaveAngleFrequency(newSineFrequency*Math.PI*2);
        if(control.isBaseWaveTimeChangeAllowed()) control.multiplyBaseWaveTime(amp);
        if(forceOnFrequency!=-1){
            double forceAmp=forceOnFrequency==0?0:control.getBaseWaveFrequency()/forceOnFrequency;
            control.setBaseWaveAngleFrequency(forceOnFrequency*Math.PI*2);
            if(control.isBaseWaveTimeChangeAllowed()) control.multiplyBaseWaveTime(forceAmp);
        }
        control.processControlParameter(deltaTime,soundData.jerkSetting);
        control.addTimeAll(deltaTime);
        return true;
    }
}