package utils;
public class Lowpass{
    private double value=0.0;
    public double process(double alpha,double num){
        value=alpha*num+(1.0-alpha)*value;
        return value;
    }
}