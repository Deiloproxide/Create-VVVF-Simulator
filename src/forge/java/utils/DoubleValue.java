package utils;
import net.minecraftforge.common.ForgeConfigSpec;
public class DoubleValue{
    private final ForgeConfigSpec.DoubleValue double_value;
    public DoubleValue(ForgeConfigSpec.DoubleValue double_value){
        this.double_value=double_value;
    }
    public Double get(){
        return double_value.get();
    }
}