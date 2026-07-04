package createvvvfsim;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import genengine.BaseSoundGen;
import genengine.SoundEngine;
import genengine.VVVFSoundGen;
import genengine.WindSoundGen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Vector3f;
import utils.AutoLoad;
import utils.Reloadable;
import utils.YamlLoader;
import vvvfsimulator.vvvf.modulation.CustomPwm;
@Mod.EventBusSubscriber(modid=Configs.mod_id,value=Dist.CLIENT)
public class ClientEvents implements Reloadable{
    private static final Minecraft mc=Minecraft.getInstance();
    private static Reloadable[] reloadables;
    private static volatile int eval_period;
    private static int eval_current;
    static{
        CustomPwm.CustomPwmPresets.preload();
    }
    @SubscribeEvent
    public static void registerCommand(RegisterClientCommandsEvent event){
        LiteralArgumentBuilder<CommandSourceStack> vvvf=Commands.literal(Configs.command_vvvf);
        LiteralArgumentBuilder<CommandSourceStack> load=Commands.literal(Configs.command_load);
        LiteralArgumentBuilder<CommandSourceStack> reload=Commands.literal(Configs.command_reload);
        RequiredArgumentBuilder<CommandSourceStack,String> path=Commands.argument(Configs.command_path,
                StringArgumentType.greedyString());
        event.getDispatcher().register(vvvf.then(load.then(path.executes(ClientEvents::onLoad))));
        event.getDispatcher().register(vvvf.then(reload.executes(ClientEvents::onReload)));
    }
    public static void registerScreen(ModContainer container){
        container.registerExtensionPoint(ConfigScreenFactory.class,
                ()->new ConfigScreenFactory(ConfigScreen::new));
    }
    @SubscribeEvent
    public static void onInit(FMLClientSetupEvent event){
        reloadables=new Reloadable[]{
                new BaseSoundGen(),new VVVFSoundGen(),new WindSoundGen(),new SoundEngine(),
                TrainData.handler,new ClientEvents(),new FSmoother(),new TrainStatus()};
        YamlLoader.loadYaml(Configs.default_yaml);
        for(Reloadable reloadable:reloadables) reloadable.reload();
    }
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event){
        SoundEngine.setAmp(mc.options.getSoundSourceVolume(SoundSource.MASTER));
        FSmoother.reloadCreate();
        String path=AutoLoad.load(mc);
        Component msg=Component.literal(YamlLoader.loadYaml(path));
        VVVFSoundGen.reloadYamlData();
        AutoLoad.save(mc,YamlLoader.success_name);
        Player player=event.getPlayer();
        player.sendSystemMessage(msg);
    }
    @SubscribeEvent
    public static void onExit(ClientPlayerNetworkEvent.LoggingOut event){
        TrainStatus.clearDataCache();
    }
    public static void onGetTrainEvent(String name,String event,String dimension,Vector3f pos){
        int x=Math.round(pos.x),y=Math.round(pos.y),z=Math.round(pos.z);
        String dimension_lang=I18n.get(Configs.dimension_path+dimension);
        String msg=I18n.get(Configs.event_path+event,name,dimension_lang,x,y,z);
        Player player=mc.player;
        if(player!=null) player.sendSystemMessage(Component.literal(msg));
    }
    public static int onLoad(CommandContext<CommandSourceStack> context){
        String path=StringArgumentType.getString(context,Configs.command_path);
        Component msg=Component.literal(YamlLoader.loadYaml(path));
        VVVFSoundGen.reloadYamlData();
        AutoLoad.save(mc,YamlLoader.success_name);
        context.getSource().sendSuccess(()->msg,false);
        return 1;
    }
    public static int onReload(CommandContext<CommandSourceStack> context){
        Component msg=Component.translatable(Configs.reload_ok);
        for(Reloadable reloadable:reloadables) reloadable.reload();
        FSmoother.reloadCreate();
        TrainStatus.reloadSpeed();
        context.getSource().sendSuccess(()->msg,false);
        return 1;
    }
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END) return;
        SoundEngine.setAmp(mc.isPaused()?0.0:mc.options.getSoundSourceVolume(SoundSource.MASTER));
        TrainStatus.tick(mc.level,mc.player);
        if(eval_current==eval_period) eval_current=0;
        TrainStatus.evalTrains(mc.level,mc.player,eval_current,eval_period);
        eval_current++;
    }
    @Override
    public void reload(){
        eval_period=Configs.eval_period.get();
        eval_current=eval_period;
    }
}