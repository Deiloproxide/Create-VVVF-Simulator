package createvvvfsim.config;
import createvvvfsim.types.SoundGenType;
import java.util.HashSet;
import java.util.Set;
public class PathConfig{
    public static final String dim_path=ModConfig.mod_id+".train.dimension.";
    public static final String event_path=ModConfig.mod_id+".train.event.";
    public static final String sound_path="train_";
    public static final Set<String> gen_path;
    static{
        gen_path=new HashSet<>();
        for(SoundGenType type:SoundGenType.values()) gen_path.add(type.name());
        gen_path.add("start");
    }
}