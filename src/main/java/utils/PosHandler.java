package utils;
import java.lang.reflect.Method;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class PosHandler{
    private static boolean has_sable=true;
    private static Instance sable_helper;
    private static Method out_project;
    static{
        try{
            Instance sable=new Instance("dev.ryanhcode.sable.Sable");
            sable_helper=sable.get("HELPER");
            out_project=sable_helper.getMethod("projectOutOfSubLevel",Level.class,Vec3.class);
        }
        catch(Throwable ignored){
            has_sable=false;
        }
    }
    public static Vec3 convert(Vec3 train_pos,Level level){
        if(has_sable){
            try{
                return sable_helper.invoke(Vec3.class,out_project,level,train_pos);
            }
            catch(ReflectiveOperationException ignored){}
        }
        return train_pos;
    }
}