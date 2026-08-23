package createvvvfsim.util;
import net.neoforged.neoforge.common.ModConfigSpec;
/**common class*/
public class Builder{
    private final ModConfigSpec.Builder builder=new ModConfigSpec.Builder();
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
    public BooleanValue define(String path,boolean defaultValue){
        return new BooleanValue(builder.define(path,defaultValue));
    }
    public ConfigSpec build(){
        return new ConfigSpec(builder.build());
    }
}