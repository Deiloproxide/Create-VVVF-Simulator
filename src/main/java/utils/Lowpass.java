package utils;
public class Lowpass{
    private double alpha,value=0.0;
    public Lowpass(double alpha){
        this.alpha=alpha;
    }
    public void setAlpha(double alpha){
        this.alpha=alpha;
    }
    public double process(double num){
        value=alpha*num+(1.0-alpha)*value;
        return value;
    }
}