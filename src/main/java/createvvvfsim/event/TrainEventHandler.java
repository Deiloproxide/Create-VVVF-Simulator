package createvvvfsim.event;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.network.EventBrd;
import createvvvfsim.types.TrainEventType;
import createvvvfsim.util.PosHandler;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
public class TrainEventHandler{
    public static void handle(Train train,TrainEventType type){
        train.speed=0.0;
        String name=train.name.getString();
        Map<ResourceKey<Level>,Vec3> positions=PosHandler.getTrainPos(train);
        for(ResourceKey<Level> dimension:positions.keySet()){
            ResourceLocation location=dimension.location();
            String dim_mod=location.getNamespace(),dim_name=location.getPath();
            Level level=ServerEvent.server.getLevel(dimension);
            Vector3f position=PosHandler.convert(positions.get(dimension),level).toVector3f();
            ServerEvent.sendEvent(new EventBrd(train.id,name,type.name(),dim_mod,dim_name,position));
        }
    }
}