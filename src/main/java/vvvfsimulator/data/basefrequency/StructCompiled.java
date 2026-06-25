package vvvfsimulator.data.basefrequency;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class StructCompiled{
    public List<Point> points=new ArrayList<>();
    public StructCompiled(Struct source){
        Struct copy=source.copy();
        copy.points.sort(Comparator.comparingInt(p->p.order));
        double currentTime=0;
        double currentFrequency=0;
        for(Struct.Point original:copy.points){
            if(original.duration==-1){
                currentFrequency=original.rate;
                continue;
            }
            double deltaTime=original.duration;
            double deltaFrequency=deltaTime*original.rate*(original.brake?-1:1);
            if(deltaTime==0) continue;
            Point point=new Point();
            point.startTime=currentTime;
            point.endTime=currentTime+deltaTime;
            point.startFrequency=currentFrequency;
            point.endFrequency=currentFrequency+deltaFrequency;
            point.isPowerOn=original.powerOn;
            point.isAccel=!original.brake;
            points.add(point);
            currentTime+=deltaTime;
            currentFrequency+=deltaFrequency;
        }
    }
    public double getEstimatedSteps(double sampleTime){
        if(points.isEmpty()) return 0;
        return points.get(points.size()-1).endTime/sampleTime;
    }
    public static class Point{
        public double startTime;
        public double endTime;
        public double startFrequency;
        public double endFrequency;
        public boolean isPowerOn=true;
        public boolean isAccel=true;
    }
}