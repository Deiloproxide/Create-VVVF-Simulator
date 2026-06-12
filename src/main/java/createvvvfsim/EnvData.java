package createvvvfsim;
import java.util.List;
public class EnvData{
    public double direct_gain=1.0;
    public double direct_cutoff=1.0;
    public ReverbSend[] sends=new ReverbSend[4];
    public double occlusion=0.0;
    public double shared_space=0.0;
    public EnvData(){
        for(int i=0;i<4;i++) sends[i]=new ReverbSend();
    }
    public class ReverbSend{
        public double gain=0.0;
        public double cutoff=1.0;
    }
    public static EnvData avg(List<EnvData> envs){
        int length=envs.size();
        if(length==0) return new EnvData();
        double avg_gain=0.0,avg_cutoff=0.0,avg_occlusion=0.0,avg_shared_space=0.0;
        double[] avg_gains=new double[]{0.0,0.0,0.0,0.0},avg_cutoffs=new double[]{0.0,0.0,0.0,0.0};
        for(EnvData env:envs){
            avg_gain+=env.direct_gain;
            avg_cutoff+=env.direct_cutoff;
            avg_occlusion+=env.occlusion;
            avg_shared_space+=env.shared_space;
            for(int i=0;i<4;i++){
                avg_gains[i]+=env.sends[i].gain;
                avg_cutoffs[i]+=env.sends[i].cutoff;
            }
        }
        EnvData avg_env=new EnvData();
        avg_env.direct_gain=avg_gain/length;
        avg_env.direct_cutoff=avg_cutoff/length;
        avg_env.occlusion=avg_occlusion/length;
        avg_env.shared_space=avg_shared_space/length;
        for(int i=0;i<4;i++){
            avg_env.sends[i].gain=avg_gains[i]/length;
            avg_env.sends[i].cutoff=avg_cutoffs[i]/length;
        }
        return avg_env;
    }
}