package utils;
import net.minecraftforge.common.ForgeConfigSpec;
/**common class*/
public class IntValue{
    private final ForgeConfigSpec.IntValue int_value;
    public IntValue(ForgeConfigSpec.IntValue int_value){
        this.int_value=int_value;
    }
    public Integer get(){
        return int_value.get();
    }
}