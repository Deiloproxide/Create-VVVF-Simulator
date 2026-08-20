package createvvvfsim.generator;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.data.TrainData;
public class SoundGen{
    protected static final int sample_rate=SpecConfig.sample_rate.get();
    protected static final int buffer_size=SpecConfig.buffer_size.get();
    protected static final double sample_dt=1.0/sample_rate;
    public void mixTo(TrainData data,double[] mix_buffer){}
}