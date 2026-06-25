package mixin;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainPacket;
import createvvvfsim.Configs;
import createvvvfsim.TrainStatus;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=TrainPacket.class,remap=false,priority=Configs.mixin_priority)
public abstract class AddTrain{
    @Shadow
    Train train;
    @Shadow
    boolean add;
    @Inject(method="handle",at=@At("RETURN"))
    private void handle(NetworkEvent.Context context,CallbackInfoReturnable<Boolean> cir){
        if(add && train!=null) TrainStatus.addTrain(train);
    }
}