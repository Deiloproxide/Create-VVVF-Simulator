package createvvvfsim.event;
import createvvvfsim.config.PathConfig;
import createvvvfsim.config.SpecConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector3f;
/**client class*/
public class EventPlayer{
    private static final Minecraft mc=Minecraft.getInstance();
    public static String getDimName(String dim_mod,String dim_name){
        if("minecraft".equals(dim_mod))
            return I18n.get(PathConfig.dim_path+dim_name);
        String key="dimension."+dim_mod+"."+dim_name;
        String fall_back=StringUtils.capitalize(dim_name.replace('_',' '));
        return Component.translatableWithFallback(key,fall_back).getString();
    }
    public static void showMsg(String name,String event,String dim_mod,String dim_name,Vector3f pos){
        int x=Math.round(pos.x),y=Math.round(pos.y),z=Math.round(pos.z);
        String dimension_lang=getDimName(dim_mod,dim_name);
        String msg=I18n.get(PathConfig.event_path+event,name,dimension_lang,x,y,z);
        Player player=mc.player;
        if(SpecConfig.mute_event.get() && player!=null)
            player.sendSystemMessage(Component.literal(msg));
    }
}