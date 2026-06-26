package soundphysics;
import createvvvfsim.Configs;
import java.lang.reflect.Method;
import utils.Instance;
public class PerfectedConst{
    public static final int[] send_delays={
            (int)(Configs.sample_rate*0.012),
            (int)(Configs.sample_rate*0.032),
            (int)(Configs.sample_rate*0.071),
            (int)(Configs.sample_rate*0.137)};
    public static final double[] send_feedbacks={
            Math.pow(10.0,-3.0*0.012/0.10),
            Math.pow(10.0,-3.0*0.032/0.35),
            Math.pow(10.0,-3.0*0.071/0.95),
            Math.pow(10.0,-3.0*0.137/1.85)};
    public static final double max_blocks;
    public static final double permeation;
    public static final float intensity;
    public static final float indoor_bias;
    public static final float outdoor_bias;
    public static final float base_gain;
    public static final float max_gain;
    public static final float gain_mul;
    public static final float hf_reduction;
    static{
        try{
            Instance configs=new Instance("com.redsmods.sound_physics_perfected.config.Config");
            Method method=configs.getMethod("getInstance");
            Instance config=Instance.invokeStatic(method);
            max_blocks=config.get(Double.class,"maxBlocksPermeated");
            permeation=config.get(Double.class,"permeationAbsorption");
            intensity=config.get(Float.class,"globalReverbIntensity");
            indoor_bias=config.get(Float.class,"indoorBias");
            outdoor_bias=config.get(Float.class,"outdoorBias");
            base_gain=config.get(Float.class,"baseReverbGain");
            max_gain=config.get(Float.class,"maxOverallGain");
            gain_mul=config.get(Float.class,"reverbGainMultiplier");
            hf_reduction=config.get(Float.class,"sendFilterHfReduction");
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}