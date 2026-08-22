package createvvvfsim.generator;
import createvvvfsim.data.BaseData;
import createvvvfsim.data.GlobalData;
import createvvvfsim.data.SlotData;
import createvvvfsim.data.TrainData;
import createvvvfsim.types.SlotType;
import createvvvfsim.types.SoundGenType;
import java.util.concurrent.ThreadLocalRandom;
public class WindSoundGen extends SoundGen{
    private static final SlotData[] slots=GlobalData.slots;
    private static final SoundGenType windgen_type=SoundGenType.wind;
    private static final SlotType base_type=SlotType.base;
    private static final int gen_index=windgen_type.ordinal();
    private static final int base_index=base_type.ordinal();
    private static final double m_2pi=2.0*Math.PI;
    @Override
    public void mixTo(TrainData data,double[] mix_buffer){
        ThreadLocalRandom tlr=ThreadLocalRandom.current();
        BaseData base=slots[data.config_from].bases[data.slots[base_index]];
        double current_amp=data.dist_amps[gen_index];
        double current_per=data.speed_pers[gen_index];
        double amp_step=(data.far_amp-current_amp)/buffer_size;
        double per_step=(data.speed_per-current_per)/buffer_size;
        double total_t=data.total_t;
        if(data.far_amp<1e-2 && current_amp<1e-2){
            data.speed_pers[gen_index]=data.speed_per;
            return;
        }
        if(data.speed_per<1e-2 && current_per<1e-2){
            data.dist_amps[gen_index]=data.far_amp;
            return;
        }
        data.pink.set(base.pink.mu,base.pink.sigma,base.pink.range);
        data.shear.set(base.shear.mu,base.shear.sigma,base.shear.range);
        data.bg_hpf.set(Math.exp(-m_2pi*base.cutoff_f*sample_dt));
        data.noise.set(base.center_f,base.cauchy_gamma);
        for(int i=0;i<buffer_size;i++){
            current_per+=per_step;
            current_amp+=amp_step;
            current_per=Math.max(current_per,0.0);
            double lowpass_alpha=1.0-Math.exp(-m_2pi*data.shear.step()*sample_dt);
            double bg_lfo=0.5+0.5*Math.sin(m_2pi*base.bg.f*total_t);
            double bg_amp=Math.min(0.5,base.bg_amp*(1.0+base.bg.depth*bg_lfo));
            data.bg_lpf.set(lowpass_alpha);
            double lpf_value=data.bg_lpf.process(data.pink.step());
            double bg_wind=data.bg_hpf.process(lpf_value*bg_amp);
            double main_lfo=0.5+0.5*Math.sin(m_2pi*base.main.f*total_t);
            double main_wind=data.noise.step()*(1.0+base.main.depth*main_lfo);
            double bg_factor=base.bg_wind_amp*current_per;
            double main_factor=base.main_wind_amp*Math.pow(current_per,1.5);
            mix_buffer[i]+=(bg_wind*bg_factor+main_wind*main_factor)*current_amp;
            total_t+=sample_dt;
        }
        data.dist_amps[gen_index]=current_amp;
        data.speed_pers[gen_index]=current_per;
        data.total_t=total_t;
    }
}