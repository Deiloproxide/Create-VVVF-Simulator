package createvvvfsim.mixin;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainStatus;
import createvvvfsim.config.ModConfig;
import createvvvfsim.event.TrainEventHandler;
import createvvvfsim.types.TrainEventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/**server class*/
@Mixin(value=TrainStatus.class,remap=false,priority=ModConfig.mixin_priority)
public class TrainEvent{
    @Shadow
    Train train;
    @Inject(method="failedMigration",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void failedMigration(CallbackInfo ci){
        TrainEventHandler.handle(train,TrainEventType.miss);
    }
    @Inject(method="highStress",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void highStress(CallbackInfo ci){
        TrainEventHandler.handle(train,TrainEventType.stress);
    }
    @Inject(method="doublePortal",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void doublePortal(CallbackInfo ci){
        TrainEventHandler.handle(train,TrainEventType.portal);
    }
    @Inject(method="endOfTrack",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/TrainStatus;displayInformation(Ljava/lang/String;Z[Ljava/lang/Object;)V"))
    private void endOfTrack(CallbackInfo ci){
        TrainEventHandler.handle(train,TrainEventType.end);
    }
    @Inject(method="crash",at=@At("HEAD"))
    private void crash(CallbackInfo ci){
        TrainEventHandler.handle(train,TrainEventType.crash);
    }
}