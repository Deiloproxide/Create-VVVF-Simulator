package genengine;
import createvvvfsim.Configs;
import utils.RandomWalk;
public class BaseSoundGen extends SoundGen{
    private static final double current_f=Configs.base_current_f;
    private static final double base_amp=Configs.base_amp;
    private static final double brown_amp=Configs.brown_amp;
    private final RandomWalk brown=new RandomWalk(0.0,Configs.brown_sigma,Configs.brown_range);
    private double phase=0.0;
    @Override
    public void mixTo(double[] mix_buffer){
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            current_amp+=amp_step;
            if(current_amp<1e-2) continue;
            double base=2*Math.abs(phase-1.0)-1.0;
            mix_buffer[i]+=(base*base_amp+brown.step()*brown_amp)*current_amp;
            phase+=2.0*current_f*sample_dt;
            if(phase>=2.0) phase-=2.0;
        }
    }
}