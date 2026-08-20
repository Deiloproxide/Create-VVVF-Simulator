package createvvvfsim.signal;
/**client class*/
public class Highpass{
    private double alpha=0.0,value=0.0,pre_num=0.0;
    public void set(double alpha){
        this.alpha=alpha;
    }
    public double process(double num){
        value=alpha*(value+num-pre_num);
        pre_num=num;
        return value;
    }
}