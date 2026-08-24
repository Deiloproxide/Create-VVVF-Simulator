package createvvvfsim.event;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.network.SpeedSync;
public class SpeedSyncHandler{
    public static void handle(){
        for(Train train:Create.RAILWAYS.trains.values())
            ServerEvent.syncSpeed(new SpeedSync(train.id,Math.abs(train.speed)*20.0));
    }
}