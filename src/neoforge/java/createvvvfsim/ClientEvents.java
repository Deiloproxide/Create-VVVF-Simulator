package createvvvfsim;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import genengine.BaseSoundGen;
import genengine.SoundEngine;
import genengine.VVVFSoundGen;
import genengine.WindSoundGen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import utils.Reloadable;
import vvvfsimulator.vvvf.modulation.CustomPwm;
@EventBusSubscriber(modid=Configs.mod_id,value=Dist.CLIENT)
public class ClientEvents implements Reloadable{
    private static final Minecraft mc=Minecraft.getInstance();
    private static Reloadable[] reloadables;
    private static volatile int eval_period;
    private static int eval_current;
    static{
        CustomPwm.CustomPwmPresets.preload();
    }
    @SubscribeEvent
    public static void registerModel(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar=event.registrar(Configs.version).optional();
        registrar.playToClient(TrainSyncModel.model_type,TrainSyncModel.stream_codec,ClientEvents::onGetSpeed);
    }
    @SubscribeEvent
    public static void registerCommand(RegisterClientCommandsEvent event){
        LiteralArgumentBuilder<CommandSourceStack> vvvf=Commands.literal(Configs.command_vvvf),
                reload=Commands.literal(Configs.command_reload);
        event.getDispatcher().register(vvvf.then(reload.executes(ClientEvents::onReload)));
    }
    public static void registerScreen(ModContainer container){
        container.registerExtensionPoint(IConfigScreenFactory.class,ConfigurationScreen::new);
    }
    @SubscribeEvent
    public static void onLoad(FMLClientSetupEvent event){
        reloadables=new Reloadable[]{
                new BaseSoundGen(),new VVVFSoundGen(),new WindSoundGen(),new SoundEngine(),
                TrainData.handler,new ClientEvents(),new FSmoother(),new TrainStatus()};
        for(Reloadable reloadable:reloadables) reloadable.reload();
    }
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event){
        FSmoother.reloadCreate();
        SoundEngine.setAmp(mc.options.getSoundSourceVolume(SoundSource.MASTER));
    }
    @SubscribeEvent
    public static void onExit(ClientPlayerNetworkEvent.LoggingOut event){
        TrainStatus.clearDataCache();
    }
    @SubscribeEvent
    public static void onPauseChange(ClientPauseChangeEvent.Post event){
        SoundEngine.setAmp(event.isPaused()?0.0:mc.options.getSoundSourceVolume(SoundSource.MASTER));
    }
    public static void onGetSpeed(TrainSyncModel model,IPayloadContext ignored){
        TrainStatus.getServerSpeed(model.train_id(),model.speed());
    }
    public static int onReload(CommandContext<CommandSourceStack> context){
        Component msg=Component.literal(Configs.command_return);
        for(Reloadable reloadable:reloadables) reloadable.reload();
        FSmoother.reloadCreate();
        TrainStatus.reloadSpeed();
        context.getSource().sendSuccess(()->msg,false);
        return 1;
    }
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event){
        TrainStatus.tick(mc.level,mc.player);
        if(eval_current>=eval_period) eval_current=0;
        TrainStatus.evalTrains(mc.level,mc.player,eval_current,eval_period);
        eval_current++;
    }
    @Override
    public void reload(){
        eval_period=Configs.eval_period.get();
        eval_current=eval_period;
    }
}