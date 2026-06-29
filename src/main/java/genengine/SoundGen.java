package genengine;
import createvvvfsim.Configs;
import utils.Reloadable;
public class SoundGen implements Reloadable{
    protected static final int sample_rate=Configs.sample_rate.get();
    protected static final int buffer_size=Configs.buffer_size.get();
    protected static final double sample_dt=1.0/sample_rate;
    protected volatile double target_amp=0.0;
    protected double current_amp=0.0;
    public void setAmp(double distance_amp){
        target_amp=distance_amp;
    }
    public void mixTo(double[] mix_buffer){}
    @Override
    public void reload(){}
}