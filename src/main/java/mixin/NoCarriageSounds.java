package mixin;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageSounds;
import createvvvfsim.Configs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(value={CarriageContraptionEntity.class},remap=false,priority=Configs.mixin_priority)
public class NoCarriageSounds{
    @Redirect(method={"tickContraption()V"},at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/CarriageSounds;tick(Lcom/simibubi/create/content/trains/entity/Carriage$DimensionalCarriageEntity;)V"))
    private void noTick(CarriageSounds sounds,Carriage.DimensionalCarriageEntity dce){}
}