package genengine;
import createvvvfsim.Configs;
public class BaseSoundGen extends SoundGen{
    private static final double max_amp=Configs.base_max_amp;
    private static final double current_f=Configs.base_current_f;
    private double phase=0.0;
    @Override
    public void mixTo(double[] mix_buffer){
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            current_amp+=amp_step;
            mix_buffer[i]+=Math.sin(phase)*current_amp*max_amp;
            phase+=2*Math.PI*current_f*sample_dt;
            if(phase>=2.0*Math.PI) phase-=2.0*Math.PI;
        }
    }
}