package mixin;
import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.CarriageParticles;
import com.simibubi.create.content.trains.entity.CarriageSounds;
import createvvvfsim.Configs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(value=CarriageContraptionEntity.class,remap=false,priority=Configs.mixin_priority)
public class NoCarriageEffects{
    @Redirect(method="tickContraption()V",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/CarriageSounds;tick(Lcom/simibubi/create/content/trains/entity/Carriage$DimensionalCarriageEntity;)V"))
    private void noSoundTick(CarriageSounds sounds,DimensionalCarriageEntity dce){}
    @Redirect(method="tickContraption()V",at=@At(value="INVOKE",
            target="Lcom/simibubi/create/content/trains/entity/CarriageParticles;tick(Lcom/simibubi/create/content/trains/entity/Carriage$DimensionalCarriageEntity;)V"))
    private void noParticleTick(CarriageParticles particles,DimensionalCarriageEntity dce){}
}