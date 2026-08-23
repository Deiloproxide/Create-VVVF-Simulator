package createvvvfsim.event;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.data.EnvData;
import java.util.Set;
public class EnvCalc{
    private static final double buffer_size=SpecConfig.buffer_size.get();
    public static void avg(Set<EnvData> envs,EnvData avg_env){
        int length=envs.size();
        if(length==0) avg_env=new EnvData();
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
        avg_env.gain=avg_gain/length;
        avg_env.cutoff=avg_cutoff/length;
        avg_env.occlusion=avg_occlusion/length;
        avg_env.shared_space=avg_shared_space/length;
        for(int i=0;i<4;i++){
            avg_env.gains[i]=avg_gains[i]/length;
            avg_env.cutoffs[i]=avg_cutoffs[i]/length;
        }
    }
    public static void setStep(EnvData from,EnvData to,EnvData step){
        step.gain=(to.gain-from.gain)/buffer_size;
        step.cutoff=(to.cutoff-from.cutoff)/buffer_size;
        step.occlusion=(to.occlusion-from.occlusion)/buffer_size;
        step.shared_space=(to.shared_space-from.shared_space)/buffer_size;
        for(int i=0;i<4;i++){
            step.gains[i]=(to.gains[i]-from.gains[i])/buffer_size;
            step.cutoffs[i]=(to.cutoffs[i]-from.cutoffs[i])/buffer_size;
        }
    }
    public static void addStep(EnvData from,EnvData step){
        from.gain+=step.gain;
        from.cutoff+=step.cutoff;
        from.occlusion+=step.occlusion;
        from.shared_space+=step.shared_space;
        for(int i=0;i<4;i++){
            from.gains[i]+=step.gains[i];
            from.cutoffs[i]+=step.cutoffs[i];
        }
    }
    public static void jumpStep(EnvData from,EnvData to){
        from.gain=to.gain;
        from.cutoff=to.cutoff;
        from.occlusion=to.occlusion;
        from.shared_space=to.shared_space;
        for(int i=0;i<4;i++){
            from.gains[i]=to.gains[i];
            from.cutoffs[i]=to.cutoffs[i];
        }
    }
}