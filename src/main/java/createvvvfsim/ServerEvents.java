package createvvvfsim;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
@EventBusSubscriber(modid=Configs.mod_id)
public class ServerEvents{
    private static final ResourceLocation sync_id=TrainSyncModel.model_type.id();
    private static final List<ServerPlayer> all_players=new ArrayList<>();
    private static final Object player_lock=new Object();
    private static final int sync_period=Configs.sync_period;
    private static int sync_current=sync_period;
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer player=(ServerPlayer)(event.getEntity());
        if(NetworkRegistry.hasChannel(player.connection,sync_id))
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
    public static void tick(ServerTickEvent.Post event){
        if(sync_current==sync_period){
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
}