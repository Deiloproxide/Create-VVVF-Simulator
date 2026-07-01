package createvvvfsim;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
public class YamlLoader{
    private static final ResourceManager manager=Minecraft.getInstance().getResourceManager();
    private static final String mod_id=Configs.mod_id;
    private static final String strategy=Configs.strategy;
    private static final String default_yaml=Configs.default_yaml;
    public static void loadYaml(String path){
        if(path.endsWith(".yaml") || path.endsWith(".yml")) loadExpand(path);
        else loadShortYaml(path);
    }
    public static void loadExpand(String path){
        try{
            load(path);
        }
        catch(Exception ignored){
            loadDefault();
        }
    }
    public static void loadShortYaml(String path){
        try{
            load(path+".yaml");
        }
        catch(Exception ignored){
            loadShortYml(path);
        }
    }
    public static void loadShortYml(String path){
        try{
            load(path+".yml");
        }
        catch(Exception ignored){
            loadDefault();
        }
    }
    public static void loadDefault(){
        try{
            load(default_yaml);
        }
        catch(Exception ignored){}
    }
    public static void load(String full_path) throws Exception{
        ResourceLocation location=ResourceLocation.tryBuild(mod_id,strategy+full_path);
        InputStream input=manager.getResource(location).orElseThrow().open();
        //TODO
    }
}