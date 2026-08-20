package createvvvfsim.signal;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
public class CauchyNoise{
    private static final double m_2pi=2.0*Math.PI;
    private final Lowpass[] iqs=new Lowpass[4];
    private double phase=0.0,d_phi=0.0;
    public CauchyNoise(){
        Arrays.fill(iqs,new Lowpass());
    }
    public void set(double center_f,double gamma,double sample_dt){
        for(Lowpass iq:iqs) iq.set(1.0-Math.exp(-m_2pi*gamma*sample_dt));
        d_phi=m_2pi*center_f*sample_dt;
    }
    public double step(){
        ThreadLocalRandom tlr=ThreadLocalRandom.current();
        double value_i=iqs[1].process(iqs[0].process(tlr.nextGaussian()));
        double value_q=iqs[3].process(iqs[2].process(tlr.nextGaussian()));
        double value=value_i*Math.cos(phase)-value_q*Math.sin(phase);
        phase+=d_phi;
        if(phase>m_2pi) phase-=m_2pi;
        return value;
    }
}