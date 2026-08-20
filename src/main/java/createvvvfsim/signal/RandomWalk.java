package createvvvfsim.signal;
import java.util.concurrent.ThreadLocalRandom;
/**client class*/
public class RandomWalk{
    private double sigma=1.0,range=1.0,value=0.0;
    public void set(double sigma,double range){
        this.sigma=sigma;
        this.range=range;
    }
    public double step(){
        ThreadLocalRandom tlr=ThreadLocalRandom.current();
        value+=tlr.nextGaussian(0.0,sigma);
        if(value<-range) value=-2.0*range-value;
        if(value>range) value=2.0*range-value;
        return value;
    }
}