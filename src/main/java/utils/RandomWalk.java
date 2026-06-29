package utils;
import java.util.concurrent.ThreadLocalRandom;
public class RandomWalk{
    private double mu;
    private double sigma;
    private double range;
    private double value=0.0;
    public RandomWalk(double mu,double sigma,double range){
        this.mu=mu;
        this.sigma=sigma;
        this.range=range;
    }
    public void set(double mu,double sigma,double range){
        this.mu=mu;
        this.sigma=sigma;
        this.range=range;
    }
    public double step(){
        ThreadLocalRandom tlr=ThreadLocalRandom.current();
        value+=tlr.nextGaussian(mu,sigma);
        if(value<-range) value=-2.0*range-value;
        if(value>range) value=2.0*range-value;
        return value;
    }
}