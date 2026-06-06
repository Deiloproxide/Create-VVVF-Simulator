package mixin;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.Configs;
import createvvvfsim.TrainStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value={Train.class},remap=false,priority=Configs.mixin_priority)
public class AddTrain{
    @Inject(method={"<init>"},at=@At("RETURN"))
    private void onInit(CallbackInfo ci){
        TrainStatus.addTrain((Train)(Object)this);
    }
}