package vvvfsimulator.data.basefrequency;
import java.util.ArrayList;
import java.util.List;
public class Struct{
    public List<Point> points=new ArrayList<>();
    public Struct copy(){
        Struct copy=new Struct();
        for(Point point:points) copy.points.add(point.copy());
        return copy;
    }
    public double getEstimatedSteps(double sampleTime){
        double totalDuration=0;
        for(Point point:points) totalDuration+=point.duration>0?point.duration:0;
        return totalDuration/sampleTime;
    }
    public StructCompiled getCompiled(){
        return new StructCompiled(this);
    }
    public static class Point{
        public int order=0;
        public double rate=0;
        public double duration=0;
        public boolean brake=false;
        public boolean powerOn=true;
        public Point copy(){
            Point copy=new Point();
            copy.order=order;
            copy.rate=rate;
            copy.duration=duration;
            copy.brake=brake;
            copy.powerOn=powerOn;
            return copy;
        }
    }
}