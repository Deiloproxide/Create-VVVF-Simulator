package soundphysics;
import createvvvfsim.Configs;
import java.lang.reflect.Method;
public class RemasteredConst{
    public static final int[] send_delays={
            (int)(Configs.sample_rate*0.0297),
            (int)(Configs.sample_rate*0.0411),
            (int)(Configs.sample_rate*0.0677),
            (int)(Configs.sample_rate*0.0973)};
    public static final double[] send_feedbacks=new double[]{
            Math.pow(10.0,-3.0*0.0297/0.08),
            Math.pow(10.0,-3.0*0.0411/0.25),
            Math.pow(10.0,-3.0*0.0677/0.58),
            Math.pow(10.0,-3.0*0.0973/0.84)};
    public static final double max_process_distance;
    public static final float block_absorption;
    public static final float decrease_distance;
    public static final float reverb_distance;
    public static final float d_rays;
    public static final int ray_count;
    public static final int ray_bounces;
    static{
        try{
            Instance sound_physics_mod=new Instance("com.sonicether.soundphysics.SoundPhysicsMod");
            Method method=sound_physics_mod.getMethod("CONFIG");
            Instance config=Instance.invokeStatic(method);
            max_process_distance=config.get(Double.class,"maxSoundProcessingDistance");
            block_absorption=config.get(Float.class,"blockAbsorption");
            decrease_distance=config.get(Float.class,"reverbAttenuationDistance");
            reverb_distance=config.get(Float.class,"reverbDistance");
            ray_count=config.get(Integer.class,"environmentEvaluationRayCount");
            ray_bounces=config.get(Integer.class,"environmentEvaluationRayBounces");
            d_rays=1f/(ray_count*ray_bounces);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}