package createvvvfsim.util;
import net.neoforged.neoforge.common.ModConfigSpec;
/**common class*/
public class DoubleValue{
    private final ModConfigSpec.DoubleValue double_value;
    public DoubleValue(ModConfigSpec.DoubleValue double_value){
        this.double_value=double_value;
    }
    public Double get(){
        return double_value.get();
    }
}