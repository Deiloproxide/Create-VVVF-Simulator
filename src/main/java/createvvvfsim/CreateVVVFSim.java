package createvvvfsim;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import soundphysics.remastered.SoundPhysicsBridgeManager;
import vvvfsimulator.vvvf.modulation.CustomPwm;
@Mod(CreateVVVFSim.mod_id)
public class CreateVVVFSim{
    public static final String mod_id="create_vvvf_simulator";
    static{
        SoundPhysicsBridgeManager.init();
        CustomPwm.CustomPwmPresets.preload();
    }
    @EventBusSubscriber(modid=mod_id,value=Dist.CLIENT)
    public static class ClientEvents{
        private static final Minecraft mc=Minecraft.getInstance();
        @SubscribeEvent
        public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event){
            SoundEngine.setMainAmp(mc.options.getSoundSourceVolume(SoundSource.MASTER));
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
}