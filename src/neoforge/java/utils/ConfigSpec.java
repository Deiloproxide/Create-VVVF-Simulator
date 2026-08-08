package utils;
import net.neoforged.neoforge.common.ModConfigSpec;
/**common class*/
public class ConfigSpec{
    private final ModConfigSpec config_spec;
    public ConfigSpec(ModConfigSpec config_spec){
        this.config_spec=config_spec;
    }
    public ModConfigSpec get(){
        return config_spec;
    }
}