package soundphysics.remastered;
import java.util.HashMap;
import java.util.Map;
import net.neoforged.fml.ModList;
import soundphysics.Handler;
public class HandlerPhysics extends Handler{
    public static final Map<String,RegisteredClass> classes=new HashMap<>();
    public static boolean register(){
        if(!ModList.get().isLoaded("sound_physics_remastered")) return false;
        try{
            //register all methods necessary
        }
        catch(Throwable ignored){
            return false;
        }
        return true;
    }
    @Override
    public void handle(double[] mix_buffer){
        //TODO
    }
}