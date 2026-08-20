package createvvvfsim.signal;
/**client class*/
public class Lowpass{
    private double alpha=0.0,value=0.0;
    public void set(double alpha){
        this.alpha=alpha;
    }
    public double process(double num){
        value=alpha*num+(1.0-alpha)*value;
        return value;
    }
}