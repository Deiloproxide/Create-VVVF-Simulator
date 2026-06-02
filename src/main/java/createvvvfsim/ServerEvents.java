package createvvvfsim;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
@EventBusSubscriber(modid=CreateVVVFSim.mod_id)
public class ServerEvents{
    private static final int tick_period=3;
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event){
        MinecraftServer server=event.getServer();
        if(server.getTickCount()%tick_period!=0) return;
        for(Train train:Create.RAILWAYS.trains.values()){
            TrainSyncModel model=new TrainSyncModel(train.id,Math.abs(train.speed)*20.0);
            PacketDistributor.sendToAllPlayers(model);
        }
    }
}