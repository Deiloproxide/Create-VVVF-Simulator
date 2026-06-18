package soundphysics;
import createvvvfsim.Configs;
import createvvvfsim.EnvData;
import createvvvfsim.TrainData;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
public class PerfectedHandler extends Handler{
    private static final double far_distance=Configs.far_distance;
    private static Method count_blocks;
    private static Method reverb_strength;
    private static Method reverb_denom;
    private static Method outdoor_leak;
    private static Method leak_denom;
    private static Method weighted_strength;
    private static Method early_reflection;
    private static Method enhanced_reverb;
    public static boolean register(){
        if(!ModList.get().isLoaded("sound_physics_perfected")) return false;
        try{
            Instance raycasting_helper=new Instance("com.redsmods.sound_physics_perfected.RaycastingHelper");
            count_blocks=raycasting_helper.getMethod(
                    "countBlocksBetween",Level.class,Vec3.class,Vec3.class,Player.class);
            reverb_strength=raycasting_helper.getMethod("getReverbStrength");
            reverb_denom=raycasting_helper.getMethod("getReverbDenom");
            outdoor_leak=raycasting_helper.getMethod("getOutdoorLeak");
            leak_denom=raycasting_helper.getMethod("getOutdoorLeakDenom");
            weighted_strength=raycasting_helper.getMethod("getWeightedReverbStrength");
            early_reflection=raycasting_helper.getMethod("getEarlyReflectionRatio");
            enhanced_reverb=raycasting_helper.getMethod("getEnhancedReverbData");
        }
        catch(Throwable ignored){
            return false;
        }
        return true;
    }
    @Override
    public EnvData getEnv(Level level,Player player,Vec3 train_pos){
        EnvData env_data=new EnvData();
        double distance=train_pos.distanceTo(player.position());
        try{
            double blocks=Instance.invokeStatic(Double.class,count_blocks,
                    level,player.getEyePosition(),train_pos,player);
            double occlusion=Math.clamp(blocks/Math.max(PerfectedConst.max_blocks,1e-6),0.0,1.0);
            double direct_gain=Math.pow(Math.clamp(PerfectedConst.permeation,0.0,1.0),blocks);
            double direct_cutoff=Math.exp(-occlusion*2.6);
            env_data.gain=direct_gain*Math.pow(direct_cutoff,0.08);
            env_data.cutoff=direct_cutoff;
            env_data.occlusion=occlusion;
            double reverb_strength=Instance.invokeStatic(Double.class,PerfectedHandler.reverb_strength)/
                    Instance.invokeStatic(Double.class,reverb_denom);
            double weighted_reverb=Instance.invokeStatic(Double.class,weighted_strength)/
                    Instance.invokeStatic(Double.class,reverb_denom);
            double outdoor_leak=Instance.invokeStatic(Double.class,PerfectedHandler.outdoor_leak)/
                    Instance.invokeStatic(Double.class,leak_denom);
            double early_ratio=Math.clamp(Instance.invokeStatic(Double.class,early_reflection),0.0,1.0);
            Instance reverb=Instance.invokeStatic(enhanced_reverb);
            double rt60=reverb.get(Double.class,"rt60");
            double early_delay=reverb.get(Double.class,"earlyReflectionDelay");
            double late_strength=reverb.get(Double.class,"lateReflectionStrength");
            double room=reverb.get(Double.class,"roomSize");
            double absorb=Math.clamp(reverb.get(Double.class,"absorption"),0.0,1.0);
            boolean indoors=reverb.get(Boolean.class,"isIndoors");
            double bias=indoors?PerfectedConst.indoor_bias:PerfectedConst.outdoor_bias;
            double enclosure=Math.clamp((indoors?1.0-outdoor_leak:reverb_strength-outdoor_leak)*bias,0.0,1.0);
            double distance_mul=1.0-Math.clamp(distance/far_distance,0.0,1.0);
            double room_mul=Math.clamp(room/20.0,0.15,1.0);
            double overall=Math.clamp(enclosure*distance_mul*room_mul*
                    (PerfectedConst.base_gain+reverb_strength*PerfectedConst.gain_mul)*
                    PerfectedConst.intensity,0.0,PerfectedConst.max_gain);
            double hf=Math.clamp((1.0-absorb)*PerfectedConst.hf_reduction*(0.35+0.65*enclosure),0.04,1.0);
            env_data.shared_space=enclosure*64.0;
            double early_weight=Math.clamp(early_ratio+1.0/(1.0+early_delay*80.0),0.0,1.0);
            double late_weight=Math.clamp(late_strength+rt60/8.0,0.0,1.0);
            double[] weights={early_weight,weighted_reverb,late_weight,late_weight};
            for(int i=0;i<4;i++){
                double darken=1.0-i*0.12;
                double rt_weight=i==3?Math.clamp(rt60/4.0,0.25,1.5):1.0;
                double send_weight=Math.clamp((0.45-0.1*i)+(0.55+0.1*i)*weights[i]*rt_weight,0.0,1.0);
                env_data.gains[i]=Math.clamp(overall*send_weight,0.0,1.0);
                env_data.cutoffs[i]=Math.clamp(hf*darken+env_data.cutoff*(1.0-darken),0.02,1.0);
            }
        }
        catch(Throwable ignored){}
        return env_data;
    }
    /*
    TODO
    @Override
    public void handle(double[] mix_buffer,List<TrainData> train_datas){}
    */
}