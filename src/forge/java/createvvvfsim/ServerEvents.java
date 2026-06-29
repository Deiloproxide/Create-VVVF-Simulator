package createvvvfsim;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.PacketDistributor;
import utils.Reloadable;
@Mod.EventBusSubscriber(modid=Configs.mod_id)
public class ServerEvents implements Reloadable{
    private static final List<ServerPlayer> all_players=new ArrayList<>();
    private static final Object player_lock=new Object();
    private static final Reloadable reloadable=new ServerEvents();
    private static volatile int sync_period;
    private static int sync_current;
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer player=(ServerPlayer)(event.getEntity());
        if(TrainSyncModel.channel.isRemotePresent(player.connection.connection))
            synchronized(player_lock){
                all_players.add(player);
            }
    }
    @SubscribeEvent
    public static void onExit(PlayerEvent.PlayerLoggedOutEvent event){
        synchronized(player_lock){
            all_players.remove((ServerPlayer)(event.getEntity()));
        }
    }
    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event){
        if(Configs.mod_id.equals(event.getConfig().getModId())){
            if(event.getConfig().getType()==ModConfig.Type.SERVER){
                reloadable.reload();
            }
        }
    }
    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event){
        if(Configs.mod_id.equals(event.getConfig().getModId())){
            if(event.getConfig().getType()==ModConfig.Type.SERVER){
                reloadable.reload();
            }
        }
    }
    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END) return;
        if(sync_current==sync_period){
            sync_current=0;
            List<ServerPlayer> players;
            synchronized(player_lock){
                players=new ArrayList<>(all_players);
            }
            for(Train train:Create.RAILWAYS.trains.values()){
                TrainSyncModel model=new TrainSyncModel(train.id,Math.abs(train.speed)*20.0);
                for(ServerPlayer player:players)
                    TrainSyncModel.channel.send(PacketDistributor.PLAYER.with(()->player),model);
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