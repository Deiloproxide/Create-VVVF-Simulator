package soundphysics;
import createvvvfsim.Configs;
import java.lang.reflect.Field;
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
            Class<?> sound_physics_mod=Class.forName("com.sonicether.soundphysics.SoundPhysicsMod");
            Object config=sound_physics_mod.getField("CONFIG").get(null);
            max_process_distance=((Number)getValue(config,"maxSoundProcessingDistance")).doubleValue();
            block_absorption=((Number)getValue(config,"blockAbsorption")).floatValue();
            decrease_distance=((Number)getValue(config,"reverbAttenuationDistance")).floatValue();
            reverb_distance=((Number)getValue(config,"reverbDistance")).floatValue();
            ray_count=((Number)getValue(config,"environmentEvaluationRayCount")).intValue();
            ray_bounces=((Number)getValue(config,"environmentEvaluationRayBounces")).intValue();
            d_rays=1f/(ray_count*ray_bounces);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    private static Object getValue(Object config,String field_name) throws ReflectiveOperationException{
        Field field=config.getClass().getField(field_name);
        Object entry=field.get(config);
        return entry.getClass().getMethod("get").invoke(entry);
    }
}