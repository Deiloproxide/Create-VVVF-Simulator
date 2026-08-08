package mixin;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainStatus;
import createvvvfsim.Configs;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import createvvvfsim.ServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import utils.PosHandler;
import utils.TrainEventType;
/**server class*/
@Mixin(value=TrainStatus.class,remap=false,priority=Configs.mixin_priority)
public class TrainEvents{
    @Shadow
    Train train;
    private void sendEvent(TrainEventType type){
        train.speed=0.0;
        Map<ResourceKey<Level>,BlockPos> dim_pos=new HashMap<>();
        for(Carriage carriage:train.carriages){
            Map<ResourceKey<Level>,DimensionalCarriageEntity> entities=((CarriageAccessor) carriage).entities();
            for(Entry<ResourceKey<Level>,DimensionalCarriageEntity> entry:entities.entrySet()){
                ResourceKey<Level> dimension=entry.getKey();
                dim_pos.putIfAbsent(dimension,BlockPos.containing(entry.getValue().positionAnchor));
            }
        }
        for(ResourceKey<Level> dimension:dim_pos.keySet()){
            ResourceLocation location=dimension.location();
            Level level=ServerEvents.server.getLevel(dimension);
            Vec3 pos=PosHandler.convert(Vec3.atLowerCornerOf(dim_pos.get(dimension)),level);
            ServerEvents.onTrainEvent(train,type.name(),location.getNamespace(),location.getPath(),pos.toVector3f());
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
    @Inject(method="crash",at=@At("HEAD"))
    private void crash(CallbackInfo ci){
        sendEvent(TrainEventType.crash);
    }
}