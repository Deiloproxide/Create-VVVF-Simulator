package utils;
import net.minecraftforge.common.ForgeConfigSpec;
/**common class*/
public class Builder{
    private final ForgeConfigSpec.Builder builder=new ForgeConfigSpec.Builder();
    public void push(String path){
        builder.push(path);
    }
    public void pop(){
        builder.pop();
    }
    public IntValue defineInRange(String path,int defaultValue,int min,int max){
        return new IntValue(builder.defineInRange(path,defaultValue,min,max));
    }
    public DoubleValue defineInRange(String path,double defaultValue,double min,double max){
        return new DoubleValue(builder.defineInRange(path,defaultValue,min,max));
    }
    public ConfigSpec build(){
        return new ConfigSpec(builder.build());
    }
}