package signal;
import java.util.concurrent.ThreadLocalRandom;
public class RandomWalk{
    private double value=0.0;
    public double step(double sigma,double range){
        ThreadLocalRandom tlr=ThreadLocalRandom.current();
        value+=tlr.nextGaussian(0.0,sigma);
        if(value<-range) value=-2.0*range-value;
        if(value>range) value=2.0*range-value;
        return value;
    }
}