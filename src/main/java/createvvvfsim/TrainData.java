package createvvvfsim;
import com.simibubi.create.content.trains.entity.Train;
import genengine.BaseSoundGen;
import genengine.VVVFSoundGen;
import genengine.WindSoundGen;
import java.util.Arrays;
import soundphysics.Handler;
import soundphysics.PerfectedHandler;
import soundphysics.RemasteredHandler;
import utils.Lowpass;
public class TrainData{
    public static final Handler handler;
    public final Train train;
    public final BaseSoundGen base_gen=new BaseSoundGen();
    public final VVVFSoundGen vvvf_gen=new VVVFSoundGen();
    public final WindSoundGen wind_gen=new WindSoundGen();
    public final FSmoother f_smoother=new FSmoother();
    public volatile EnvData target_env=new EnvData();
    public final EnvData current_env=new EnvData();
    public final EnvData env_step=new EnvData();
    public final Lowpass[] filters=new Lowpass[5];
    public boolean use_server=false,server_reloaded=false;
    public boolean is_last_valid=false,is_last_move=false;
    public int reload_timer=0;
    static{
        if(RemasteredHandler.register()) handler=new RemasteredHandler();
        else if(PerfectedHandler.register()) handler=new PerfectedHandler();
        else handler=new Handler();
    }
    public TrainData(Train train){
        Arrays.setAll(filters,i->new Lowpass());
        this.train=train;
    }
    public void set(double speed,double near_factor,double far_factor,boolean valid,boolean move){
        double smoothed=f_smoother.smoothF(speed);
        base_gen.setAmp(near_factor);
        vvvf_gen.setAmp(near_factor);
        wind_gen.setAmp(far_factor);
        vvvf_gen.setF(smoothed);
        wind_gen.setF(smoothed);
        is_last_valid=valid;
        is_last_move=move;
    }
    public void setStep(int buffer_size){
        EnvData from=current_env,to=target_env;
        env_step.gain=(to.gain-from.gain)/buffer_size;
        env_step.cutoff=(to.cutoff-from.cutoff)/buffer_size;
        env_step.occlusion=(to.occlusion-from.occlusion)/buffer_size;
        env_step.shared_space=(to.shared_space-from.shared_space)/buffer_size;
        for(int i=0;i<4;i++){
            env_step.gains[i]=(to.gains[i]-from.gains[i])/buffer_size;
            env_step.cutoffs[i]=(to.cutoffs[i]-from.cutoffs[i])/buffer_size;
        }
    }
    public void addStep(){
        current_env.gain+=env_step.gain;
        current_env.cutoff+=env_step.cutoff;
        current_env.occlusion+=env_step.occlusion;
        current_env.shared_space+=env_step.shared_space;
        for(int i=0;i<4;i++){
            current_env.gains[i]+=env_step.gains[i];
            current_env.cutoffs[i]+=env_step.cutoffs[i];
        }
    }
}