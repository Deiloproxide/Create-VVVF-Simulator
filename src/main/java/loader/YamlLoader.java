package loader;
import createvvvfsim.Configs;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import vvvfsimulator.data.vvvf.Manager;
import vvvfsimulator.loader.LoadContext;
import vvvfsimulator.loader.LoadException;
/**client class*/
public class YamlLoader{
    private static final ResourceManager manager=Minecraft.getInstance().getResourceManager();
    private static final List<LoadException> parse_errors=Arrays.asList(LoadException.lex,
            LoadException.parse,LoadException.compose,LoadException.dump,LoadException.init);
    public static String success_name;
    public static String loadYaml(String path){
        List<String> err_list=new ArrayList<>();
        String[] paths;
        String status_path;
        int status_length,status_ptr;
        if(path.endsWith(".yaml") || path.endsWith(".yml")){
            status_length=1;
            paths=new String[]{path};
        }
        else{
            status_length=2;
            paths=new String[]{path+".yaml",path+".yml"};
        }
        for(status_ptr=0;status_ptr<status_length;status_ptr++){
            LoadContext context=load(paths[status_ptr]);
            LoadException exception=context.exception;
            String key=Configs.yaml_exception_path+exception.name();
            if(parse_errors.contains(exception))
                err_list.add(I18n.get(key,paths[status_ptr],context.row,context.col));
            else err_list.add(I18n.get(key,paths[status_ptr]));
            if(exception==LoadException.normal) break;
        }
        StringBuilder msg=new StringBuilder(),err_msg=new StringBuilder();
        if(status_ptr==status_length){
            status_path=Configs.status_path+"error";
            success_name=null;
            for(String err:err_list) err_msg.append("\n").append(err);
            msg.append(I18n.get(status_path,path,err_msg.toString()));
        }
        else{
            status_path=Configs.status_path+"ok";
            success_name=paths[status_ptr];
            msg.append(I18n.get(status_path,paths[status_ptr]));
        }
        return msg.toString();
    }
    private static LoadContext load(String path){
        ResourceLocation location=ResourceLocation.tryBuild(Configs.group_id,Configs.strategy+path);
        if(location==null) return new LoadContext(LoadException.invalid,0,0);
        LoadContext context;
        try(InputStream stream=manager.getResource(location).orElseThrow().open()){
            context=Manager.load(path,stream);
        }
        catch(NoSuchElementException ignored){
            context=new LoadContext(LoadException.notfound,0,0);
        }
        catch(IOException ignored){
            context=new LoadContext(LoadException.io,0,0);
        }
        return context;
    }
}