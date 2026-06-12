package mixin;
import com.simibubi.create.content.trains.entity.AddTrainPacket;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.Configs;
import createvvvfsim.TrainStatus;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=AddTrainPacket.class,remap=false,priority=Configs.mixin_priority)
public abstract class AddTrain{
    @Shadow
    public abstract Train train();
    @Inject(method="handle",at=@At("RETURN"))
    private void handle(LocalPlayer player,CallbackInfo ci){
        TrainStatus.addTrain(train());
    }
}