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
        double amp_step=(data.dist_amp-current_amp)/buffer_size;
        double per_step=(data.speed_per-current_per)/buffer_size;
        double total_t=data.total_t;
        if(data.dist_amp<1e-2 && current_amp<1e-2){
            data.speed_pers[gen_index]=data.speed_per;
            return;
        }
        if(data.speed_per<1e-2 && current_per<1e-2){
            data.dist_amps[gen_index]=data.dist_amp;
            return;
        }
        data.pink_bg.set(1.0-base.pink_r0);
        data.bg_shear.set(base.bg_shear_rate*Math.sqrt(sample_dt),base.bg_shear_range);
        data.bg_hpf.set(Math.exp(-m_2pi*base.hp_cutoff*sample_dt));
        data.noise.set(base.main_center_f,base.main_cauchy_gamma,sample_dt);
        for(int i=0;i<buffer_size;i++){
            current_per+=per_step;
            current_amp+=amp_step;
            current_per=Math.max(current_per,0.0);
            double bg_shear_value=data.bg_shear.step();
            double lowpass_alpha=1.0-Math.exp(-2.0*Math.PI*(base.bg_shear_base+bg_shear_value)*sample_dt);
            double bg_lfo=0.5+0.5*Math.sin(2.0*Math.PI*base.wind_mod_f*total_t);
            double current_pink_bg=data.pink_bg.process(tlr.nextGaussian()*0.5);
            double bg_amp=Math.min(0.5,base.wind_base_amp*(1.0+base.wind_mod_depth*bg_lfo));
            data.bg_lpf.set(lowpass_alpha);
            double lowpass_value=data.bg_lpf.process(current_pink_bg);
            double bg_wind=data.bg_hpf.process(lowpass_value*bg_amp);
            double main_lfo=0.5+0.5*Math.sin(2.0*Math.PI*base.main_mod_f*total_t);
            double current_main_wind=data.noise.step()*(1.0+base.main_mod_depth*main_lfo);
            double bg_factor=base.bg_wind_amp*current_per;
            double main_factor=base.main_wind_amp*Math.pow(current_per,1.5);
            mix_buffer[i]+=(bg_wind*bg_factor+current_main_wind*main_factor)*current_amp;
            total_t+=sample_dt;
        }
        data.dist_amps[gen_index]=current_amp;
        data.speed_pers[gen_index]=current_per;
        data.total_t=total_t;
    }
}