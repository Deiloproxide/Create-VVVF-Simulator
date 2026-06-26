package createvvvfsim;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import genengine.SoundEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPauseChangeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import vvvfsimulator.vvvf.modulation.CustomPwm;
@EventBusSubscriber(modid=Configs.mod_id,value=Dist.CLIENT)
public class ClientEvents{
    private static final Minecraft mc=Minecraft.getInstance();
    private static final int eval_period=Configs.eval_period;
    private static int eval_current=eval_period;
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
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event){
        FSmoother.reloadMaxSpeed();
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
        FSmoother.reloadMaxSpeed();
        TrainStatus.forceReload();
        context.getSource().sendSuccess(()->msg,false);
        return 1;
    }
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event){
        TrainStatus.tick(mc.level,mc.player);
        if(eval_current==eval_period) eval_current=0;
        TrainStatus.evalTrains(mc.level,mc.player,eval_current);
        eval_current++;
    }
}