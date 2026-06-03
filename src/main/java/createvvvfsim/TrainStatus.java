package createvvvfsim;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import java.util.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public class TrainStatus{
    private static final double max_distance=Configs.max_distance;
    public static final Map<UUID,Double> cached_speeds=new HashMap<>();
    public static final List<TrainData> all_trains=new ArrayList<>();
    public static final Object speed_lock=new Object(),train_lock=new Object();
    public static void addTrain(Train train){
        synchronized(train_lock){
            all_trains.add(new TrainData(train));
        }
    }
    public static void getServerSpeed(TrainSyncModel model,IPayloadContext context){
        synchronized(speed_lock){
            cached_speeds.put(model.train_id(),model.speed());
        }
    }
    public static void clearDataCache(){
        synchronized(train_lock){
            all_trains.clear();
        }
        synchronized(speed_lock){
            cached_speeds.clear();
        }
    }
    public static void forceReload(){
        synchronized(train_lock){
            for(TrainData train_data:all_trains){
                Double speed;
                synchronized(speed_lock){
                    speed=cached_speeds.get(train_data.train.id);
                }
                if(speed!=null){
                    train_data.f_smoother.reloadF(speed);
                    train_data.is_reloaded=true;
                }
            }
        }
    }
    public static void tick(Level level,Player player){
        synchronized(train_lock){
            all_trains.removeIf(data->data.train.invalid);
            if(player==null) return;
            Vec3 player_pos=player.position();
            for(TrainData train_data:all_trains){
                double total_factor=0.0;
                Double speed;
                for(Carriage carriage:train_data.train.carriages){
                    DimensionalCarriageEntity dce=carriage.getDimensionalIfPresent(level.dimension());
                    if(dce==null) continue;
                    CarriageContraptionEntity entity=dce.entity.get();
                    if(entity==null) continue;
                    if(entity.isRemoved()) continue;
                    Vec3 train_pos=entity.position();
                    double distance=train_pos.distanceTo(player_pos);
                    total_factor+=Math.max(0.0,1.0-distance/max_distance);
                }
                synchronized(speed_lock){
                    speed=cached_speeds.get(train_data.train.id);
                }
                if(!train_data.is_reloaded){
                    if(speed==null) speed=0.0;
                    else{
                        train_data.f_smoother.reloadF(speed);
                        train_data.is_reloaded=true;
                    }
                }
                train_data.is_move=speed>1e-2;
                if(train_data.is_move && !train_data.is_last_move && total_factor>1e-2){
                    level.playLocalSound(player,SoundEvents.LAVA_EXTINGUISH,
                            SoundSource.NEUTRAL,0.75f*(float)total_factor,1f);
                    level.playLocalSound(player,SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                            SoundSource.NEUTRAL,0.6f*(float)total_factor,1.5f);
                }
                train_data.gen.setAmp(total_factor);
                train_data.vvvf_gen.setAmp(total_factor);
                train_data.vvvf_gen.setF(train_data.f_smoother.smoothF(speed));
                train_data.is_last_move=train_data.is_move;
            }
        }
    }
}