package createvvvfsim.event;
import createvvvfsim.data.BaseData;
import createvvvfsim.data.GlobalData;
import createvvvfsim.data.SlotData;
import createvvvfsim.data.TrainData;
import createvvvfsim.types.SlotType;
import java.util.Map;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
public class SoundsPlayer{
    private static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> sounds=SoundsEvent.sounds;
    private static final SlotData[] slots=GlobalData.slots;
    private static final SlotType base_type=SlotType.base;
    private static final int base_index=base_type.ordinal();
    public static void play(TrainData data,Level level,Vec3 player_pos,
                            double near_factor,boolean is_move){
        double x=player_pos.x,y=player_pos.y,z=player_pos.z;
        if(near_factor>1e-2){
            level.playLocalSound(x,y,z,sounds.get("base").value(),SoundSource.NEUTRAL,
                    1f,1f,false);
            if(is_move){
                level.playLocalSound(x,y,z,sounds.get("vvvf").value(),SoundSource.NEUTRAL,
                        1f,1f,false);
                if(!data.is_last_move){
                    BaseData base=slots[data.config_from].bases[data.slots[base_index]];
                    level.playLocalSound(x,y,z,SoundEvents.LAVA_EXTINGUISH,SoundSource.NEUTRAL,
                            (float)(base.begin_amp*1.5*near_factor),2f,false);
                    level.playLocalSound(x,y,z,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.NEUTRAL,
                            (float)(base.begin_amp*near_factor),2f,false);
                    level.playLocalSound(x,y,z,sounds.get("start").value(),SoundSource.NEUTRAL,
                            1f,1f,false);
                }
            }
        }
        if(!data.is_mute)
            level.playLocalSound(x,y,z,sounds.get("wind").value(),SoundSource.NEUTRAL,
                    1f,1f,false);
    }
}