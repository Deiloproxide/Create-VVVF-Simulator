package createvvvfsim.signal;
import createvvvfsim.config.SpecConfig;
import java.util.concurrent.ThreadLocalRandom;
/**client class*/
public class RandomWalk{
    private static final double sample_rate=SpecConfig.sample_rate.get();
    private static final double sample_dt=1.0/sample_rate;
    private double mu=0.0,sigma=1.0,range=1.0,value=0.0;
    public void set(double mu,double sigma,double range){
        this.mu=mu;
        this.sigma=sigma*Math.sqrt(sample_dt);
        this.range=range;
    }
    public double step(){
        ThreadLocalRandom tlr=ThreadLocalRandom.current();
        value+=tlr.nextGaussian(mu,sigma);
        if(value<mu-range) value=-2.0*range-value;
        if(value>mu+range) value=2.0*range-value;
        return value;
    }
}