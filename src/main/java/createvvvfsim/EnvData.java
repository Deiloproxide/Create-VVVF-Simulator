package createvvvfsim;
import java.util.Arrays;
import java.util.List;
public class EnvData{
    public double gain=1.0,cutoff=1.0;
    public double[] gains={0.0,0.0,0.0,0.0},cutoffs={1.0,1.0,1.0,1.0};
    public double occlusion=0.0;
    public double shared_space=0.0;
    public static EnvData avg(List<EnvData> envs){
        int length=envs.size();
        if(length==0) return new EnvData();
        double avg_gain=0.0,avg_cutoff=0.0,avg_occlusion=0.0,avg_shared_space=0.0;
        double[] avg_gains={0.0,0.0,0.0,0.0},avg_cutoffs={0.0,0.0,0.0,0.0};
        for(EnvData env:envs){
            avg_gain+=env.gain;
            avg_cutoff+=env.cutoff;
            avg_occlusion+=env.occlusion;
            avg_shared_space+=env.shared_space;
            for(int i=0;i<4;i++){
                avg_gains[i]+=env.gains[i];
                avg_cutoffs[i]+=env.cutoffs[i];
            }
        }
        EnvData avg_env=new EnvData();
        avg_env.gain=avg_gain/length;
        avg_env.cutoff=avg_cutoff/length;
        avg_env.occlusion=avg_occlusion/length;
        avg_env.shared_space=avg_shared_space/length;
        for(int i=0;i<4;i++){
            avg_env.gains[i]=avg_gains[i]/length;
            avg_env.cutoffs[i]=avg_cutoffs[i]/length;
        }
        return avg_env;
    }
}