package soundphysics;
import java.lang.reflect.Method;
public class PerfectedConst{
    public static final double max_blocks;
    public static final double permeation;
    public static final double intensity;
    public static final double indoor_bias;
    public static final double outdoor_bias;
    public static final double base_gain;
    public static final double max_gain;
    public static final double gain_mul;
    public static final double hf_reduction;
    static{
        try{
            Instance configs=new Instance("com.redsmods.sound_physics_perfected.config.Config");
            Method method=configs.getMethod("getInstance");
            Instance config=Instance.invokeStatic(method);
            max_blocks=config.get(Double.class,"maxBlocksPermeated");
            permeation=config.get(Double.class,"permeationAbsorption");
            intensity=config.get(Double.class,"globalReverbIntensity");
            indoor_bias=config.get(Double.class,"indoorBias");
            outdoor_bias=config.get(Double.class,"outdoorBias");
            base_gain=config.get(Double.class,"baseReverbGain");
            max_gain=config.get(Double.class,"maxOverallGain");
            gain_mul=config.get(Double.class,"reverbGainMultiplier");
            hf_reduction=config.get(Double.class,"sendFilterHfReduction");
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}