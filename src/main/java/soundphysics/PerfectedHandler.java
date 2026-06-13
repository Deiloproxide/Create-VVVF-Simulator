package soundphysics;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
public class PerfectedHandler extends Handler{
    private static Method count_blocks_between;
    private static Method get_config;
    private static Method get_distance_from_wall_echo;
    private static Method get_distance_from_wall_echo_denom;
    private static Method get_reverb_strength;
    private static Method get_reverb_denom;
    private static Method get_outdoor_leak;
    private static Method get_outdoor_leak_denom;
    private static Method get_weighted_reverb_strength;
    private static Method get_early_reflection_ratio;
    private static Method get_enhanced_reverb_data;
    private static Field max_blocks_permeated;
    private static Field permeation_absorption;
    private static Field reverb;
    private static Field global_reverb_intensity;
    private static Field indoor_bias;
    private static Field outdoor_bias;
    private static Field base_reverb_gain;
    private static Field reverb_gain_multiplier;
    private static Field max_overall_gain;
    private static Field send_filter_hf_reduction;
    private static Field rt60;
    private static Field early_reflection_delay;
    private static Field late_reflection_strength;
    private static Field room_size;
    private static Field absorption;
    private static Field is_indoors;
    public static boolean register(){
        if(!ModList.get().isLoaded("sound_physics_perfected")) return false;
        try{
            Class<?> raycasting_helper=Class.forName("com.redsmods.sound_physics_perfected.RaycastingHelper");
            count_blocks_between=raycasting_helper.getMethod(
                    "countBlocksBetween",Level.class,Vec3.class,Vec3.class,Player.class);
            get_distance_from_wall_echo=raycasting_helper.getMethod("getDistanceFromWallEcho");
            get_distance_from_wall_echo_denom=raycasting_helper.getMethod("getDistanceFromWallEchoDenom");
            get_reverb_strength=raycasting_helper.getMethod("getReverbStrength");
            get_reverb_denom=raycasting_helper.getMethod("getReverbDenom");
            get_outdoor_leak=raycasting_helper.getMethod("getOutdoorLeak");
            get_outdoor_leak_denom=raycasting_helper.getMethod("getOutdoorLeakDenom");
            get_weighted_reverb_strength=raycasting_helper.getMethod("getWeightedReverbStrength");
            get_early_reflection_ratio=raycasting_helper.getMethod("getEarlyReflectionRatio");
            get_enhanced_reverb_data=raycasting_helper.getMethod("getEnhancedReverbData");
            Class<?> config=Class.forName("com.redsmods.sound_physics_perfected.config.Config");
            get_config=config.getMethod("getInstance");
            max_blocks_permeated=config.getField("maxBlocksPermeated");
            permeation_absorption=config.getField("permeationAbsorption");
            reverb=config.getField("reverb");
            global_reverb_intensity=config.getField("globalReverbIntensity");
            indoor_bias=config.getField("indoorBias");
            outdoor_bias=config.getField("outdoorBias");
            base_reverb_gain=config.getField("baseReverbGain");
            reverb_gain_multiplier=config.getField("reverbGainMultiplier");
            max_overall_gain=config.getField("maxOverallGain");
            send_filter_hf_reduction=config.getField("sendFilterHfReduction");
            Class<?> enhanced_reverb_data=Class.forName(
                    "com.redsmods.sound_physics_perfected.ReverbHelpers.EnhancedReverbData");
            rt60=enhanced_reverb_data.getField("rt60");
            early_reflection_delay=enhanced_reverb_data.getField("earlyReflectionDelay");
            late_reflection_strength=enhanced_reverb_data.getField("lateReflectionStrength");
            room_size=enhanced_reverb_data.getField("roomSize");
            absorption=enhanced_reverb_data.getField("absorption");
            is_indoors=enhanced_reverb_data.getField("isIndoors");
            get_config.invoke(null);
        }
        catch(Throwable ignored){
            return false;
        }
        return true;
    }
}