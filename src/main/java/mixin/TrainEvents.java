package mixin;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainStatus;
import createvvvfsim.Configs;
import createvvvfsim.ServerEvents;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import utils.PosHandler;
import utils.TrainEventType;
@Mixin(value=TrainStatus.class,remap=false,priority=Configs.mixin_priority)
public class TrainEvents{
    @Shadow
    Train train;
    private static final Map<ResourceKey<Level>,String> name=new HashMap<>();
    static{
        name.put(Level.OVERWORLD,"overworld");
        name.put(Level.NETHER,"nether");
        name.put(Level.END,"end");
    }
    private void sendEvent(TrainEventType type){
        train.speed=0.0;
        for(ResourceKey<Level> dimension:train.getPresentDimensions()){
            Vec3 train_pos=null;
            for(Carriage carriage:train.carriages){
                DimensionalCarriageEntity dce=carriage.getDimensionalIfPresent(dimension);
                if(dce==null) continue;
                CarriageContraptionEntity entity=dce.entity.get();
                if(entity==null) continue;
                if(entity.isRemoved()) continue;
                train_pos=PosHandler.convert(dce.positionAnchor,entity.level());
                break;
            }
            if(train_pos!=null)
                ServerEvents.onTrainEvent(train,type.name(),name.get(dimension),train_pos.toVector3f());
        }
    }
    @Inject(method="failedMigration",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void failedMigration(CallbackInfo ci){
        sendEvent(TrainEventType.miss);
    }
    @Inject(method="highStress",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void highStress(CallbackInfo ci){
        sendEvent(TrainEventType.stress);
    }
    @Inject(method="doublePortal",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void doublePortal(CallbackInfo ci){
        sendEvent(TrainEventType.portal);
    }
    @Inject(method="endOfTrack",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void endOfTrack(CallbackInfo ci){
        sendEvent(TrainEventType.end);
    }
    @Inject(method="crash",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;addMessage(Lcom/simibubi/create/content/trains/entity/TrainStatus$StatusMessage;)V"))
    private void crash(CallbackInfo ci){
        sendEvent(TrainEventType.crash);
    }
}