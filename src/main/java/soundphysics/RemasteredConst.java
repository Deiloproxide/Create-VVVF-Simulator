package soundphysics;
import createvvvfsim.Configs;
import java.lang.reflect.Method;
import utils.Instance;
public class RemasteredConst{
    private static final int sample_rate=Configs.sample_rate.get();
    public static final int[] send_delays={
            (int)(sample_rate*0.0297),
            (int)(sample_rate*0.0411),
            (int)(sample_rate*0.0677),
            (int)(sample_rate*0.0973)};
    public static final double[] send_feedbacks={
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
            Instance config=sound_physics_mod.get("CONFIG");
            max_process_distance=getConfig(Double.class,config,"maxSoundProcessingDistance");
            block_absorption=getConfig(Float.class,config,"blockAbsorption");
            decrease_distance=getConfig(Float.class,config,"reverbAttenuationDistance");
            reverb_distance=getConfig(Float.class,config,"reverbDistance");
            ray_count=getConfig(Integer.class,config,"environmentEvaluationRayCount");
            ray_bounces=getConfig(Integer.class,config,"environmentEvaluationRayBounces");
            d_rays=1f/(ray_count*ray_bounces);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    private static <T> T getConfig(Class<T> clazz,Instance config,String field_name)
            throws ReflectiveOperationException{
        Instance config_entry=config.get(field_name);
        Method method_get=config_entry.getMethod("get");
        return config_entry.invoke(clazz,method_get);
    }
}