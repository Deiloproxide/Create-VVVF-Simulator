package createvvvfsim;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.EnvData.ReverbSend;
import genengine.BaseSoundGen;
import genengine.VVVFSoundGen;
import genengine.WindSoundGen;
public class TrainData{
    public final Train train;
    public final BaseSoundGen base_gen=new BaseSoundGen();
    public final VVVFSoundGen vvvf_gen=new VVVFSoundGen();
    public final WindSoundGen wind_gen=new WindSoundGen();
    public final FSmoother f_smoother=new FSmoother();
    public volatile EnvData target_env=new EnvData();
    public final EnvData current_env=new EnvData();
    public final EnvData env_step=new EnvData();
    public boolean is_reloaded=false,is_move=false,is_last_move=false;
    public double filter=0.0;
    public double[] filters=new double[]{0.0,0.0,0.0,0.0};
    public TrainData(Train train){
        this.train=train;
    }
    public void set(double speed,double near_factor,double far_factor){
        double smoothed=f_smoother.smoothF(speed);
        base_gen.setAmp(near_factor);
        vvvf_gen.setAmp(near_factor);
        wind_gen.setAmp(far_factor);
        vvvf_gen.setF(smoothed);
        wind_gen.setF(smoothed);
    }
    public void setStep(int buffer_size){
        EnvData from=current_env,to=target_env;
        env_step.direct_gain=(to.direct_gain-from.direct_gain)/buffer_size;
        env_step.direct_cutoff=(to.direct_cutoff-from.direct_cutoff)/buffer_size;
        env_step.occlusion=(to.occlusion-from.occlusion)/buffer_size;
        env_step.shared_space=(to.shared_space-from.shared_space)/buffer_size;
        for(int i=0;i<4;i++){
            env_step.sends[i].gain=(to.sends[i].gain-from.sends[i].gain)/buffer_size;
            env_step.sends[i].cutoff=(to.sends[i].cutoff-from.sends[i].cutoff)/buffer_size;
        }
    }
    public void addStep(){
        current_env.direct_gain+=env_step.direct_gain;
        current_env.direct_cutoff+=env_step.direct_cutoff;
        current_env.occlusion+=env_step.occlusion;
        current_env.shared_space+=env_step.shared_space;
        for(int i=0;i<4;i++){
            current_env.sends[i].gain+=env_step.sends[i].gain;
            current_env.sends[i].cutoff+=env_step.sends[i].cutoff;
        }
    }
    public void lowPass(double sample){
        ReverbSend[] sends=current_env.sends;
        filter+=(sample-filter)*Math.clamp(current_env.direct_cutoff,0.02,1.0);
        for(int i=0;i<4;i++)
            filters[i]+=(sample*sends[i].gain-filters[i])*Math.clamp(sends[i].cutoff,0.02,1.0);
    }
}