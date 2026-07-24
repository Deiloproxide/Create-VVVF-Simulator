package loader;
import createvvvfsim.Configs;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import vvvfsimulator.generation.audio.trainsound.AudioResourceManager;
public class IRLoader{
    private static final ResourceManager manager=Minecraft.getInstance().getResourceManager();
    public static String success_name;
    public static String loadIR(String path){
        List<String> err_list=new ArrayList<>();
        String[] paths;
        LoadStatus[] statuses;
        int status_length,status_ptr;
        if(path.endsWith(".ir") || path.endsWith(".wav")){
            status_length=2;
            paths=new String[]{path,Configs.default_ir};
            statuses=new LoadStatus[]{LoadStatus.ok,LoadStatus.fallback,LoadStatus.error};
        }
        else if(path.endsWith(".mp3") || path.endsWith(".flac") || path.endsWith(".ogg")){
            status_length=1;
            paths=new String[]{Configs.default_ir};
            statuses=new LoadStatus[]{LoadStatus.fallback,LoadStatus.error};
            String key=Configs.ir_exception_path+LoadException.unsupported.name();
            err_list.add(I18n.get(key,path));
        }
        else{
            status_length=3;
            paths=new String[]{path+".ir",path+".wav",Configs.default_ir};
            statuses=new LoadStatus[]{LoadStatus.ok,LoadStatus.ok,LoadStatus.fallback,LoadStatus.error};
        }
        for(status_ptr=0;status_ptr<status_length;status_ptr++){
            LoadContext context=load(paths[status_ptr]);
            LoadException exception=context.exception;
            String key=Configs.ir_exception_path+exception.name();
            err_list.add(I18n.get(key,paths[status_ptr]));
            if(exception==LoadException.normal) break;
        }
        LoadStatus status=statuses[status_ptr];
        if(status==LoadStatus.error) success_name=null;
        else success_name=paths[status_ptr];
        StringBuilder msg=new StringBuilder(),err_msg=new StringBuilder();
        String status_path=Configs.ir_status_path+status.name();
        switch(status){
            case ok:
                msg.append(I18n.get(status_path,paths[status_ptr]));
                break;
            case fallback:
                for(String err:err_list) err_msg.append("\n").append(err);
                msg.append(I18n.get(status_path,path,err_msg.toString()));
                break;
            case error:
                for(String err:err_list) err_msg.append("\n").append(err);
                msg.append(I18n.get(status_path,path,path,err_msg.toString()));
                break;
        }
        return msg.toString();
    }
    private static LoadContext load(String path){
        ResourceLocation location=ResourceLocation.tryBuild(Configs.group_id,Configs.irsound+path);
        if(location==null) return new LoadContext(LoadException.invalid,0,0);
        LoadContext context;
        try(InputStream stream=manager.getResource(location).orElseThrow().open()){
            context=AudioResourceManager.load(stream,path.endsWith(".ir"));
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