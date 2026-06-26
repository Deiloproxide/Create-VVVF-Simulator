package utils;
public class Highpass{
    private double alpha,value=0.0,pre_num=0.0;
    public Highpass(double alpha){
        this.alpha=alpha;
    }
    public void setAlpha(double alpha){
        this.alpha=alpha;
    }
    public double process(double num){
        value=alpha*(value+num-pre_num);
        pre_num=num;
        return value;
    }
}