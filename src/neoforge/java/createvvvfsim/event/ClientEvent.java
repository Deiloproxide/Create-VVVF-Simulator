package createvvvfsim.event;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.engine.SoundEngine;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import vvvfsimulator.vvvf.modulation.CustomPwm;
@EventBusSubscriber(modid=ModConfig.mod_id,value=Dist.CLIENT)
public class ClientEvent{
    private static final Minecraft mc=Minecraft.getInstance();
    private static boolean is_single=false,is_ready=false;
    private static volatile int eval_period;
    private static int eval_current;
    static{
        CustomPwm.CustomPwmPresets.preload();
    }
    public static void registerScreen(ModContainer container){
        container.registerExtensionPoint(IConfigScreenFactory.class,ConfigurationScreen::new);
    }
    @SubscribeEvent
    public static void onSoundInit(SoundEngineLoadEvent event){
        if(is_ready) SoundEngine.load();
    }
    @SubscribeEvent
    public static void onInit(FMLClientSetupEvent event){
        eval_period=SpecConfig.eval_period.get();
        is_ready=true;
    }
    @SubscribeEvent
    public static void onJoin(LoggingIn event){
        SoundEngine.load();
        is_single=mc.isSingleplayer();
        SpeedCalc.loadCreate();
        //load config
    }
    @SubscribeEvent
    public static void onExit(LoggingOut event){
        Controller.clearCache();
    }
    @SubscribeEvent
    public static void onPauseChange(ClientPauseChangeEvent.Post event){
        SoundEngine.setPause(event.isPaused() && is_single);
    }
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event){
        Controller.tick(mc.player);
        if(eval_current>=eval_period) eval_current=0;
        Controller.eval(mc.player,eval_current,eval_period);
        eval_current++;
    }
}