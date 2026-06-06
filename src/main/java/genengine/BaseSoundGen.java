package genengine;
import createvvvfsim.Configs;
import java.util.concurrent.ThreadLocalRandom;
public class BaseSoundGen extends SoundGen{
    private static final double current_f=Configs.base_current_f;
    private static final double base_max_amp=Configs.base_max_amp;
    private static final double brown_amp=Configs.brown_amp;
    private static final double brown_sigma=Configs.brown_sigma;
    private static final double brown_range=Configs.brown_range;
    private ThreadLocalRandom tlr;
    private double phase=0.0;
    private double value=0.0;
    private double step(){
        value+=tlr.nextGaussian(0.0,brown_sigma);
        if(value<-brown_range) value=-2.0*brown_range-value;
        if(value>brown_range) value=2.0*brown_range-value;
        return value;
    }
    @Override
    public void mixTo(double[] mix_buffer){
        tlr=ThreadLocalRandom.current();
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            current_amp+=amp_step;
            if(current_amp<1e-2) continue;
            double base=2*Math.abs(phase-1.0)-1.0;
            mix_buffer[i]+=(base+brown_amp*step())*current_amp*base_max_amp;
            phase+=2.0*current_f*sample_dt;
            if(phase>=2.0) phase-=2.0;
        }
    }
}