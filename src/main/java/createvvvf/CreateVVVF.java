package createvvvf;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import vvvf.modulation.CustomPwm;
@Mod(CreateVVVF.mod_id)
public class CreateVVVF{
    public static final String mod_id="create_vvvf";
    public static final Minecraft mc=Minecraft.getInstance();
    public CreateVVVF(){
        CustomPwm.CustomPwmPresets.preload();
        VVVFSoundEngine.offPause(mc.options.getSoundSourceVolume(SoundSource.MASTER));
    }
    @EventBusSubscriber(modid=mod_id,value=Dist.CLIENT)
    public static class ClientEvents{
        private static boolean is_paused=false,is_last_paused=false;
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event){
            is_paused=mc.isPaused();
            if(is_paused && !is_last_paused) VVVFSoundEngine.onPause();
            if(!is_paused && is_last_paused)
                VVVFSoundEngine.offPause(mc.options.getSoundSourceVolume(SoundSource.MASTER));
            is_last_paused=is_paused;
        }
    }
}