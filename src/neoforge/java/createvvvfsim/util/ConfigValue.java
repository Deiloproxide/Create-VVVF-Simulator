package createvvvfsim.util;
import net.neoforged.neoforge.common.ModConfigSpec;
/**common class*/
public class ConfigValue<T>{
    private final ModConfigSpec.ConfigValue<T> config_value;
    public ConfigValue(ModConfigSpec.ConfigValue<T> config_value){
        this.config_value=config_value;
    }
    public T get(){
        return config_value.get();
    }
}