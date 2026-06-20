package createvvvfsim;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import java.util.*;
import mixin.ISyncAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public class TrainStatus{
    private static final double near_distance=Configs.near_distance;
    private static final double far_distance=Configs.far_distance;
    private static final double main_amp=Configs.main_amp;
    private static final double gas_amp=Configs.gas_amp;
    private static final double switch_amp=Configs.switch_amp;
    private static final int eval_period=Configs.eval_period;
    public static final Object speed_lock=new Object(),train_lock=new Object();
    public static final Map<UUID,Double> cached_speeds=new HashMap<>();
    private static final List<TrainData> all_trains=new ArrayList<>();
    private static List<TrainData> eval_trains=new ArrayList<>();
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
    public static List<TrainData> getTrainData(){
        List<TrainData> train_datas;
        synchronized(train_lock){
            train_datas=new ArrayList<>(all_trains);
        }
        return train_datas;
    }
    public static void forceReload(){
        List<TrainData> train_datas=getTrainData();
        for(TrainData train_data:train_datas){
            train_data.server_reloaded=false;
            train_data.reload_timer=1;
        }
    }
    public static void tick(Level level,Player player){
        if(level==null) return;
        Vec3 player_pos=player.position();
        synchronized(train_lock){
            all_trains.removeIf(data->data.train.invalid);
        }
        List<TrainData> train_datas=getTrainData();
        for(TrainData train_data:train_datas){
            double near_factor=0.0,far_factor=0.0,avg_speed=0.0;
            int carriage_count=0;
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
                ISyncAccessor sync=(ISyncAccessor)(entity.getCarriageData());
                float[] wheel_distances=sync.pointDistanceSnapshot();
                double avg_distance=0.0;
                for(int i=0;i<Math.min(wheel_distances.length,2);i++)
                    avg_distance+=Math.abs(wheel_distances[i]);
                if(wheel_distances.length==0) continue;
                else avg_distance/=2;
                int sync_period=entity.getType().updateInterval();
                double sync_f=1.0/sync_period;
                int ticks_since=sync.ticksSince();
                if(ticks_since>=sync_period*2) sync_f/=ticks_since-sync_period*2+1;
                sync_f*=ServerSpeedProvider.get();
                avg_speed+=avg_distance*sync_f*20.0;
                carriage_count++;
            }
            if(!train_data.use_server)
                synchronized(speed_lock){
                    if(cached_speeds.containsKey(train_data.train.id)) train_data.use_server=true;
                }
            double speed;
            boolean is_valid=carriage_count!=0;
            if(train_data.use_server){
                synchronized(speed_lock){
                    speed=cached_speeds.get(train_data.train.id);
                }
                if(!train_data.server_reloaded){
                    train_data.f_smoother.reloadF(speed);
                    train_data.server_reloaded=true;
                }
            }
            else{
                speed=is_valid?avg_speed/carriage_count:0.0;
                if(!is_valid && train_data.is_last_valid) train_data.f_smoother.reloadF(0.0);
                if(is_valid && !train_data.is_last_valid)train_data.reload_timer=10;
                if(train_data.reload_timer>0){
                    train_data.reload_timer--;
                    if(train_data.reload_timer==0) train_data.f_smoother.reloadF(speed);
                }
            }
            if(train_data.train.derailed){
                speed=0.0;
                train_data.f_smoother.reloadF(0.0);
            }
            boolean is_move=speed>1e-2;
            if(is_move && !train_data.is_last_move && near_factor>1e-2){
                level.playLocalSound(player,SoundEvents.LAVA_EXTINGUISH,SoundSource.NEUTRAL,
                        (float)(main_amp*gas_amp*near_factor),2f);
                level.playLocalSound(player,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.NEUTRAL,
                        (float)(main_amp*switch_amp*near_factor),2f);
            }
            train_data.set(speed,near_factor,far_factor,is_valid,is_move);
        }
    }
    public static void evalTrains(Level level,Player player,int period_state){
        if(level==null) return;
        if(period_state==0) eval_trains=getTrainData();
        for(int i=period_state;i<eval_trains.size();i+=eval_period){
            TrainData train_data=eval_trains.get(i);
            List<Carriage> carriages=train_data.train.carriages;
            List<EnvData> envs=new ArrayList<>();
            for(Carriage carriage:carriages){
                DimensionalCarriageEntity dce=carriage.getDimensionalIfPresent(level.dimension());
                if(dce==null) continue;
                CarriageContraptionEntity entity=dce.entity.get();
                if(entity==null) continue;
                if(entity.isRemoved()) continue;
                envs.add(TrainData.handler.getEnv(level,player,entity.position()));
            }
            train_data.target_env=EnvData.avg(envs);
        }
    }
}