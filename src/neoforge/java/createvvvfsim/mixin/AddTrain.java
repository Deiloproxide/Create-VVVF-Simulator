package createvvvfsim.mixin;
import com.simibubi.create.content.trains.entity.AddTrainPacket;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.config.ModConfig;
import createvvvfsim.event.Controller;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/**client class*/
@Mixin(value=AddTrainPacket.class,remap=false,priority=ModConfig.mixin_priority)
public abstract class AddTrain{
    @Shadow
    public abstract Train train();
    @Inject(method="handle",at=@At("RETURN"))
    private void handle(LocalPlayer player,CallbackInfo ci){
        Controller.addTrain(train());
    }
}