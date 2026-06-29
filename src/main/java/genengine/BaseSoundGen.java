package genengine;
import createvvvfsim.Configs;
import utils.RandomWalk;
public class BaseSoundGen extends SoundGen{
    private static volatile double current_f;
    private static volatile double base_amp;
    private static volatile double brown_amp;
    private static volatile double brown_sigma;
    private static volatile double brown_range;
    private final RandomWalk brown=new RandomWalk();
    private double phase=0.0;
    @Override
    public void mixTo(double[] mix_buffer){
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            current_amp+=amp_step;
            if(current_amp<1e-2) continue;
            double base=2*Math.abs(phase-1.0)-1.0;
            double brown_value=brown.step(brown_sigma,brown_range);
            mix_buffer[i]+=(base*base_amp+brown_value*brown_amp)*current_amp;
            phase+=2.0*current_f*sample_dt;
            if(phase>=2.0) phase-=2.0;
        }
    }
    @Override
    public void reload(){
        current_f=Configs.base_current_f.get();
        base_amp=Configs.base_amp.get();
        brown_amp=Configs.brown_amp.get();
        brown_sigma=Configs.brown_sigma.get();
        brown_range=Configs.brown_range.get();
    }
}