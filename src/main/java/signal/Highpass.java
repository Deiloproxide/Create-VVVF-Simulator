package signal;
public class Highpass{
    private double value=0.0,pre_num=0.0;
    public double process(double alpha,double num){
        value=alpha*(value+num-pre_num);
        pre_num=num;
        return value;
    }
}