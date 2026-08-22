package createvvvfsim.generator;
import createvvvfsim.data.BaseData;
import createvvvfsim.data.GlobalData;
import createvvvfsim.data.SlotData;
import createvvvfsim.data.TrainData;
import createvvvfsim.types.SlotType;
import createvvvfsim.types.SoundGenType;
public class BaseSoundGen extends SoundGen{
    private static final SlotData[] slots=GlobalData.slots;
    private static final SoundGenType basegen_type=SoundGenType.base;
    private static final SlotType base_type=SlotType.base;
    private static final int gen_index=basegen_type.ordinal();
    private static final int base_index=base_type.ordinal();
    private static final double m_2pi=2.0*Math.PI;
    @Override
    public void mixTo(TrainData data,double[] mix_buffer){
        BaseData base=slots[data.config_from].bases[data.slots[base_index]];
        double current_amp=data.dist_amps[gen_index];
        double amp_step=(data.near_amp-current_amp)/buffer_size;
        double phase=data.phase;
        if(data.near_amp<1e-2 && current_amp<1e-2) return;
        data.brown.set(base.brown.mu,base.brown.sigma,base.brown.range);
        for(int i=0;i<buffer_size;i++){
            current_amp+=amp_step;
            double harmonics=0;
            for(int j=0;j<4;j++) harmonics+=base.base_amps[j]*Math.sin((j+1)*phase+base.base_phases[j]);
            double brown_value=data.brown.step();
            mix_buffer[i]+=(harmonics*base.base_amp+brown_value*base.brown_amp)*data.dist_amps[gen_index];
            phase+=m_2pi*base.base_f*sample_dt;
            if(phase>m_2pi) phase-=m_2pi;
        }
        data.dist_amps[gen_index]=current_amp;
        data.phase=phase;
    }
}