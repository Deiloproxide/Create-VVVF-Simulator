package createvvvfsim.event;
import createvvvfsim.data.BaseData;
import createvvvfsim.data.GlobalData;
import createvvvfsim.data.SlotData;
import createvvvfsim.data.TrainData;
import createvvvfsim.types.SlotType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class SoundEvent{
    private static final SlotData[] slots=GlobalData.slots;
    private static final SlotType base_type=SlotType.base;
    private static final int base_index=base_type.ordinal();
    public static void internalPlay(TrainData data,Level level,Vec3 player_pos,
                                    double near_factor,boolean is_move){
        if(near_factor>1e-2){
            if(is_move && !data.is_last_move){
                double x=player_pos.x,y=player_pos.y,z=player_pos.z;
                BaseData base=slots[data.config_from].bases[data.slots[base_index]];
                level.playLocalSound(x,y,z,SoundEvents.LAVA_EXTINGUISH,SoundSource.NEUTRAL,
                        (float)(base.begin_amp*1.5*near_factor),2f,false);
                level.playLocalSound(x,y,z,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.NEUTRAL,
                        (float)(base.begin_amp*near_factor),2f,false);
                //start
            }
            if(is_move){
                //vvvf
            }
            //base
        }
        if(!data.is_mute){
            //wind
        }
    }
}