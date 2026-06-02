package createvvvfsim;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid=CreateVVVFSim.mod_id,value=Dist.CLIENT)
public class ClientEvents{
    private static final Minecraft mc=Minecraft.getInstance();
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event){
        SoundEngine.setMainAmp(mc.options.getSoundSourceVolume(SoundSource.MASTER));
    }
    @SubscribeEvent
    public static void onExit(ClientPlayerNetworkEvent.LoggingOut event){
        TrainStatus.clearDataCache();
    }
    @SubscribeEvent
    public static void onPauseChange(ClientPauseChangeEvent.Post event){
        SoundEngine.setMainAmp(event.isPaused()?0.0:mc.options.getSoundSourceVolume(SoundSource.MASTER));
    }
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event){
        TrainStatus.tick(mc.level,mc.player);
    }
}