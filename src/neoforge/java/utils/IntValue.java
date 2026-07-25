package utils;
import net.neoforged.neoforge.common.ModConfigSpec;
public class IntValue{
    private final ModConfigSpec.IntValue int_value;
    public IntValue(ModConfigSpec.IntValue int_value){
        this.int_value=int_value;
    }
    public Integer get(){
        return int_value.get();
    }
}