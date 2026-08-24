package createvvvfsim.event;
import createvvvfsim.config.ModConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
@EventBusSubscriber(modid=ModConfig.mod_id,value=Dist.CLIENT)
public class CommandsEvent{
    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event){

    }
    public static void onList(){

    }
    public static void onUpload(){

    }
}
