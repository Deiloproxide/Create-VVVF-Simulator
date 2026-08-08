package utils;
import net.minecraftforge.common.ForgeConfigSpec;
/**common class*/
public class ConfigSpec{
    private final ForgeConfigSpec config_spec;
    public ConfigSpec(ForgeConfigSpec config_spec){
        this.config_spec=config_spec;
    }
    public ForgeConfigSpec get(){
        return config_spec;
    }
}