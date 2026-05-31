package createvvvfsim;
import vvvfsimulator.vvvf.MyMath;
public class SoundGen{
    protected static final int sample_rate=Configs.sample_rate;
    protected static final int buffer_size=Configs.buffer_size;
    protected static final double sample_dt=Configs.sample_dt;
    private static final double max_amp=Configs.base_max_amp;
    private static final double current_f=Configs.base_current_f;
    private double phase=0.0;
    protected volatile double target_amp=0.0;
    protected double current_amp=0.0;
    public void setAmp(double distance_amp){
        target_amp=distance_amp;
    }
    public void mixTo(double[] mix_buffer){
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            current_amp+=amp_step;
            mix_buffer[i]+=Math.sin(phase)*current_amp*max_amp;
            phase+=MyMath.M_2PI*current_f*sample_dt;
            if(phase>=2.0*Math.PI) phase-=2.0*Math.PI;
        }
    }
}