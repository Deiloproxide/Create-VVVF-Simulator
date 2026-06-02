package mixin;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.entity.CarriageSounds;
import createvvvfsim.Configs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(value={CarriageSounds.class},remap=false,priority=Configs.mixin_priority)
public class NoCarriageSounds{
    @Redirect(method={"tick(Lcom/simibubi/create/content/trains/entity/Carriage$DimensionalCarriageEntity;)V"},require=0,expect=0,
            at=@At(target="Lcom/simibubi/create/AllSoundEvents$SoundEntry;playAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;FFZ)V",value="INVOKE"))
    private void noPlayAt(AllSoundEvents.SoundEntry self,Level world,Vec3 pos,float volume,float pitch,boolean fade){}
    @Redirect(method={"tick(Lcom/simibubi/create/content/trains/entity/Carriage$DimensionalCarriageEntity;)V"},require=0,expect=0,
            at=@At(target="Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V",value="INVOKE"))
    private void noPlayLocalSound(Level self,double x,double y,double z,SoundEvent event,SoundSource source,float volume,float pitch,boolean distanceDelay){}
}