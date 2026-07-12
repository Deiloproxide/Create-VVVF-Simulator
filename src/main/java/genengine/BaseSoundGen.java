package genengine;
import createvvvfsim.Configs;
import signal.RandomWalk;
public class BaseSoundGen extends SoundGen{
    private static volatile double base_amp;
    private static volatile double brown_amp;
    private static volatile double brown_sigma;
    private static volatile double brown_range;
    private static volatile double current_f;
    private static volatile double[] base_amps,base_phases,temp;
    private final RandomWalk brown=new RandomWalk();
    private double phase=0.0;
    public double baseHarmonics(double phase){
        double base=0;
        for(int i=0;i<4;i++) base+=base_amps[i]*Math.sin((i+1)*phase+base_phases[i]);
        return base;
    }
    @Override
    public void mixTo(double[] mix_buffer){
        double amp_step=(target_amp-current_amp)/buffer_size;
        if(target_amp<1e-2 && current_amp<1e-2) return;
        for(int i=0;i<buffer_size;i++){
            current_amp+=amp_step;
            double base=baseHarmonics(Math.PI*phase);
            double brown_value=brown.step(brown_sigma,brown_range);
            mix_buffer[i]+=(base*base_amp+brown_value*brown_amp)*current_amp;
            phase+=2.0*current_f*sample_dt;
            if(phase>=1.0) phase-=2.0;
        }
    }
    @Override
    public void reload(){
        base_amp=Configs.base_amp.get();
        brown_amp=Configs.brown_amp.get();
        brown_sigma=Configs.brown_sigma.get();
        brown_range=Configs.brown_range.get();
        current_f=Configs.base_current_f.get();
        temp=new double[]{Configs.first_amp.get(),Configs.second_amp.get(),
                Configs.third_amp.get(),Configs.fourth_amp.get()};
        base_amps=temp;
        temp=new double[]{Configs.first_phase.get(),Configs.second_phase.get(),
                Configs.third_phase.get(),Configs.fourth_phase.get()};
        base_phases=temp;
    }
}