package utils;
import net.neoforged.neoforge.common.ModConfigSpec;
public class ConfigSpec{
    private final ModConfigSpec config_spec;
    public ConfigSpec(ModConfigSpec config_spec){
        this.config_spec=config_spec;
    }
    public ModConfigSpec get(){
        return config_spec;
    }
    public static class Builder{
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
        public ConfigSpec build(){
            return new ConfigSpec(builder.build());
        }
    }
    public static class IntValue{
        private final ModConfigSpec.IntValue int_value;
        public IntValue(ModConfigSpec.IntValue int_value){
            this.int_value=int_value;
        }
        public Integer get(){
            return int_value.get();
        }
    }
    public static class DoubleValue{
        private final ModConfigSpec.DoubleValue double_value;
        public DoubleValue(ModConfigSpec.DoubleValue double_value){
            this.double_value=double_value;
        }
        public Double get(){
            return double_value.get();
        }
    }
}