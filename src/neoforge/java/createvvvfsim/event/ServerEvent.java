package createvvvfsim.event;
import createvvvfsim.autoloader.MapLoader;
import createvvvfsim.autoloader.ServerLoader;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.network.EventBrd;
import createvvvfsim.network.SpeedSync;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent.Loading;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid=ModConfig.mod_id)
public class ServerEvent{
    public static MinecraftServer server;
    private static final Set<ServerPlayer> players=ConcurrentHashMap.newKeySet();
    private static volatile int sync_period;
    private static int sync_current;
    @SubscribeEvent
    public static void onInit(ServerStartingEvent event){
        server=event.getServer();
        ServerLoader.load();
        MapLoader.load();
    }
    @SubscribeEvent
    public static void onStop(ServerStoppingEvent event){
        MapLoader.save();
        ServerLoader.save();
    }
    @SubscribeEvent
    public static void onJoin(PlayerLoggedInEvent event){
        ServerPlayer player=(ServerPlayer)(event.getEntity());
        if(CommonEvent.types.stream().allMatch(player.connection::hasChannel)){
            players.add(player);
            SlotSyncHandler.handle(player);
        }
    }
    @SubscribeEvent
    public static void onExit(PlayerLoggedOutEvent event){
        players.remove((ServerPlayer)(event.getEntity()));
    }
    @SubscribeEvent
    public static void onLoad(Loading event){
        if(ModConfig.mod_id.equals(event.getConfig().getModId())){
            if(event.getConfig().getType()==Type.SERVER){
                sync_period=SpecConfig.sync_period.get();
            }
        }
    }
    @SubscribeEvent
    public static void onReload(Reloading event){
        if(ModConfig.mod_id.equals(event.getConfig().getModId())){
            if(event.getConfig().getType()==Type.SERVER){
                sync_period=SpecConfig.sync_period.get();
            }
        }
    }
    public static void sendEvent(EventBrd model){
        for(ServerPlayer player:players) PacketDistributor.sendToPlayer(player,model);
    }
    public static void syncSpeed(SpeedSync model){
        for(ServerPlayer player:players) PacketDistributor.sendToPlayer(player,model);
    }
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event){
        if(sync_current>=sync_period){
            sync_current=0;
            SpeedSyncHandler.handle();
        }
        sync_current++;
    }
}