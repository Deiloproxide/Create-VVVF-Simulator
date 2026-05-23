package mixin;
import createvvvf.VVVFSoundEngine;
import createvvvf.VVVFSoundGen;
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
@Mixin(value={CarriageSounds.class},remap=false,priority=900)
public class VVVFCarriageSounds{
    private final VVVFSoundGen gen=new VVVFSoundGen();
    private boolean is_move=false,is_last_move=false;
    @Redirect(method={"tick(Lcom/simibubi/create/content/trains/entity/Carriage$DimensionalCarriageEntity;)V"},
        at=@At(target="Lcom/simibubi/create/AllSoundEvents$SoundEntry;playAt",value="INVOKE"),
        require=0,expect=0)
    private void noPlayAt(AllSoundEvents.SoundEntry self,Level world,Vec3 pos,float volume,float pitch,boolean fade){}
    @Inject(method={"tick"},at={@At("RETURN")},remap=false)
    private void tickUpdate(Carriage.DimensionalCarriageEntity dce,CallbackInfo ci){
        CarriageContraptionEntity entity=dce.entity.get();
        if(entity!=null){
            Vec3 train_pos=entity.position(),move=train_pos.subtract(entity.getPrevPositionVec());
            is_move=move.length()>1E-2d;
            LocalPlayer player=Minecraft.getInstance().player;
            if(player!=null) gen.updateAmp(train_pos,player.position());
            gen.updateF(move);
        }
        else is_move=false;
        if(is_move && !is_last_move) VVVFSoundEngine.addPlayer(gen);
        if(!is_move && is_last_move) VVVFSoundEngine.removePlayer(gen);
        is_last_move=is_move;
    }
}