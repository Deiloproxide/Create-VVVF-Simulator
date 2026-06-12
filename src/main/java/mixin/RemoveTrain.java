package mixin;
import com.simibubi.create.content.trains.entity.RemoveTrainPacket;
import createvvvfsim.Configs;
import createvvvfsim.TrainStatus;
import java.util.UUID;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=RemoveTrainPacket.class,remap=false,priority=Configs.mixin_priority)
public abstract class RemoveTrain{
    @Shadow
    public abstract UUID id();
    @Inject(method="handle",at=@At("RETURN"))
    private void handle(LocalPlayer player,CallbackInfo ci){
        TrainStatus.removeTrain(id());
    }
}