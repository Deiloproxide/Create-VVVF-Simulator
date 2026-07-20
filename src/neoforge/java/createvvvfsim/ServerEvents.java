package createvvvfsim;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent.Loading;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import utils.Reloadable;
@EventBusSubscriber(modid=Configs.mod_id)
public class ServerEvents implements Reloadable{
    public static MinecraftServer server;
    private static final List<ServerPlayer> all_players=new ArrayList<>();
    private static final Object player_lock=new Object();
    private static final Reloadable reloadable=new ServerEvents();
    private static volatile int sync_period;
    private static int sync_current;
    @SubscribeEvent
    public static void onInit(ServerStartingEvent event){
        server=event.getServer();
    }
    @SubscribeEvent
    public static void onJoin(PlayerLoggedInEvent event){
        ServerPlayer player=(ServerPlayer)(event.getEntity());
        if(CommonEvents.types.stream().allMatch(player.connection::hasChannel))
            synchronized(player_lock){
                all_players.add(player);
            }
    }
    @SubscribeEvent
    public static void onExit(PlayerLoggedOutEvent event){
        synchronized(player_lock){
            all_players.remove((ServerPlayer)(event.getEntity()));
        }
    }
    @SubscribeEvent
    public static void onLoad(Loading event){
        if(Configs.mod_id.equals(event.getConfig().getModId())){
            if(event.getConfig().getType()==ModConfig.Type.SERVER){
                reloadable.reload();
            }
        }
    }
    @SubscribeEvent
    public static void onReload(Reloading event){
        if(Configs.mod_id.equals(event.getConfig().getModId())){
            if(event.getConfig().getType()==ModConfig.Type.SERVER){
                reloadable.reload();
            }
        }
    }
    public static void onTrainEvent(Train train,String type,String dimension,Vector3f pos){
        List<ServerPlayer> players;
        synchronized(player_lock){
            players=new ArrayList<>(all_players);
        }
        TrainEventModel model=new TrainEventModel(train.id,train.name.getString(),type,dimension,pos);
        for(ServerPlayer player:players) PacketDistributor.sendToPlayer(player,model);
    }
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event){
        if(sync_current>=sync_period){
            sync_current=0;
            List<ServerPlayer> players;
            synchronized(player_lock){
                players=new ArrayList<>(all_players);
            }
            for(Train train:Create.RAILWAYS.trains.values()){
                TrainSyncModel model=new TrainSyncModel(train.id,Math.abs(train.speed)*20.0);
                for(ServerPlayer player:players) PacketDistributor.sendToPlayer(player,model);
            }
        }
        sync_current++;
    }
    @Override
    public void reload(){
        sync_period=Configs.sync_period.get();
        sync_current=sync_period;
    }
}