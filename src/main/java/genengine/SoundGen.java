package genengine;
import createvvvfsim.Configs;
public class SoundGen{
    protected static final int sample_rate=Configs.sample_rate;
    protected static final int buffer_size=Configs.buffer_size;
    protected static final double sample_dt=1.0/sample_rate;
    protected volatile double target_amp=0.0;
    protected double current_amp=0.0;
    public void setAmp(double distance_amp){
        target_amp=distance_amp;
    }
    public void mixTo(double[] mix_buffer){}
}