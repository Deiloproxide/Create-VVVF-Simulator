package mixin;
import createvvvfsim.VVVFSoundEngine;
import createvvvfsim.VVVFSoundGen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageSounds;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value={CarriageSounds.class},remap=false,priority=1027)
public class VVVFCarriageSounds{
    private final VVVFSoundGen gen=new VVVFSoundGen();
    private static final float max_distance=64f;
    private boolean is_play=false,is_last_play=false;
    @Redirect(method={"tick(Lcom/simibubi/create/content/trains/entity/Carriage$DimensionalCarriageEntity;)V"},
        at=@At(target="Lcom/simibubi/create/AllSoundEvents$SoundEntry;playAt",value="INVOKE"),
        require=0,expect=0)
    private void noPlayAt(AllSoundEvents.SoundEntry self,Level world,Vec3 pos,float volume,float pitch,boolean fade){}
    @Inject(method={"tick"},at={@At("RETURN")},remap=false)
    private void tickUpdate(Carriage.DimensionalCarriageEntity dce,CallbackInfo ci){
        CarriageContraptionEntity entity=dce.entity.get();
        LocalPlayer player=Minecraft.getInstance().player;
        if(entity!=null && player!=null){
            Vec3 train_pos=entity.position(),move=train_pos.subtract(entity.getPrevPositionVec());
            float distance=(float)train_pos.distanceTo(player.position());
            is_play=move.length()>1E-2d && distance<max_distance;
            if(is_play && !is_last_play) VVVFSoundEngine.addPlayer(gen);
            else if(!is_play && is_last_play) VVVFSoundEngine.removePlayer(gen);
            gen.updateAmp(1f-distance/max_distance);
            gen.updateF(move);
        }
        else is_play=false;
        is_last_play=is_play;
    }
}