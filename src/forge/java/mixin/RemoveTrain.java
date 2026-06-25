package mixin;
import com.simibubi.create.content.trains.entity.TrainPacket;
import createvvvfsim.Configs;
import createvvvfsim.TrainStatus;
import java.util.UUID;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=TrainPacket.class,remap=false,priority=Configs.mixin_priority)
public abstract class RemoveTrain{
    @Shadow
    UUID trainId;
    @Shadow
    boolean add;
    @Inject(method="handle",at=@At("RETURN"))
    private void handle(NetworkEvent.Context context,CallbackInfoReturnable<Boolean> cir){
        if(!add) TrainStatus.removeTrain(trainId);
    }
}