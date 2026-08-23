package createvvvfsim.util;
import net.neoforged.neoforge.common.ModConfigSpec;
public class BooleanValue{
    private final ModConfigSpec.BooleanValue boolean_value;
    public BooleanValue(ModConfigSpec.BooleanValue boolean_value){
        this.boolean_value=boolean_value;
    }
    public Boolean get(){
        return boolean_value.get();
    }
}
