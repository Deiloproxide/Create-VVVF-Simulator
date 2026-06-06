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
    private static final double near_distance=Configs.near_distance;
    private static final double far_distance=Configs.far_distance;
    public static final Map<UUID,Double> cached_speeds=new HashMap<>();
    public static final List<TrainData> all_trains=new ArrayList<>();
    public static final Object speed_lock=new Object(),train_lock=new Object();
    public static void addTrain(Train train){
        synchronized(train_lock){
            all_trains.add(new TrainData(train));
        }
    }
    public static void removeTrain(UUID id){
        synchronized(train_lock){
            all_trains.removeIf(data->data.train.id.equals(id));
        }
        synchronized(speed_lock){
            cached_speeds.remove(id);
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
                double near_factor=0.0,far_factor=0.0;
                Double speed;
                synchronized(speed_lock){
                    speed=cached_speeds.get(train_data.train.id);
                }
                if(!train_data.is_reloaded){
                    if(speed==null) continue;
                    else{
                        train_data.f_smoother.reloadF(speed);
                        train_data.is_reloaded=true;
                    }
                }
                if(train_data.train.derailed){
                    speed=0.0;
                    train_data.f_smoother.reloadF(speed);
                }
                for(Carriage carriage:train_data.train.carriages){
                    DimensionalCarriageEntity dce=carriage.getDimensionalIfPresent(level.dimension());
                    if(dce==null) continue;
                    CarriageContraptionEntity entity=dce.entity.get();
                    if(entity==null) continue;
                    if(entity.isRemoved()) continue;
                    Vec3 train_pos=entity.position();
                    double distance=train_pos.distanceTo(player_pos);
                    near_factor+=Math.max(0.0,1.0-distance/near_distance);
                    far_factor+=Math.max(0.0,1.0-distance/far_distance);
                }
                train_data.is_move=speed>1e-2;
                if(train_data.is_move && !train_data.is_last_move && near_factor>1e-2){
                    level.playLocalSound(player,SoundEvents.LAVA_EXTINGUISH,
                            SoundSource.NEUTRAL,0.75f*(float)near_factor,1f);
                    level.playLocalSound(player,SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                            SoundSource.NEUTRAL,0.6f*(float)near_factor,1.5f);
                }
                double smoothed=train_data.f_smoother.smoothF(speed);
                train_data.base_gen.setAmp(near_factor);
                train_data.vvvf_gen.setAmp(near_factor);
                train_data.wind_gen.setAmp(far_factor);
                train_data.vvvf_gen.setF(smoothed);
                train_data.wind_gen.setF(smoothed);
                train_data.is_last_move=train_data.is_move;
            }
        }
    }
}