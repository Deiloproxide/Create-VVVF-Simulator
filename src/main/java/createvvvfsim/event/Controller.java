package createvvvfsim.event;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.data.GlobalData;
import createvvvfsim.data.TrainData;
import createvvvfsim.mixin.ISyncAccessor;
import createvvvfsim.util.PosHandler;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class Controller{
    private static final double near_distance=SpecConfig.near_distance.get();
    private static final double far_distance=SpecConfig.far_distance.get();
    private static final Set<TrainData> trains=GlobalData.trains;
    private static final Map<UUID,Double> cached_speeds=GlobalData.cached_speeds;
    private static final Set<UUID> cached_events=GlobalData.cached_events;
    private static Set<TrainData> eval_trains=new HashSet<>(trains);
    public static void addTrain(Train train){
        trains.add(new TrainData(train));
    }
    public static void removeTrain(UUID id){
        trains.removeIf(data->data.train.id.equals(id));
        cached_speeds.remove(id);
        cached_events.remove(id);
    }
    public static void getServerSpeed(UUID id,double speed){
        cached_speeds.put(id,speed);
    }
    public static void getServerEvent(UUID id){
        cached_speeds.put(id,0.0);
        cached_events.add(id);
    }
    public static void clearCache(){
        trains.clear();
        cached_speeds.clear();
        cached_events.clear();
    }
    public static Tuple<Double,Double> calcSpeed(CarriageContraptionEntity entity){
        ISyncAccessor sync=(ISyncAccessor)(entity.getCarriageData());
        float[] wheel_distances=sync.pointDistanceSnapshot();
        double avg_distance=0.0;
        for(int i=0;i<Math.min(wheel_distances.length,2);i++)
            avg_distance+=Math.abs(wheel_distances[i]);
        if(wheel_distances.length==0) return null;
        else avg_distance/=2;
        int sync_period=entity.getType().updateInterval();
        double sync_f=1.0/sync_period;
        int ticks_since=sync.ticksSince();
        if(ticks_since>=sync_period*2) sync_f/=ticks_since-sync_period*2+1;
        sync_f*=ServerSpeedProvider.get();
        return new Tuple<>(avg_distance,sync_f);
    }
    public static double speedResolver(TrainData data,double avg_speed,int carriage_cnt){
        if(!data.use_server_speed && cached_speeds.containsKey(data.train.id))
            data.use_server_speed=true;
        double speed;
        boolean is_valid=carriage_cnt!=0;
        if(data.use_server_speed){
            UUID id=data.train.id;
            speed=cached_speeds.get(id);
            if(!data.server_reloaded){
                SpeedCalc.reloadSpeed(data,speed);
                data.server_reloaded=true;
            }
            if(cached_events.contains(id)){
                speed=0.0;
                SpeedCalc.reloadSpeed(data,speed);
                cached_events.remove(id);
            }
        }
        else{
            speed=is_valid?avg_speed/carriage_cnt:0.0;
            if(!is_valid && data.is_last_valid) SpeedCalc.reloadSpeed(data,0.0);
            if(is_valid && !data.is_last_valid) data.reload_timer=10;
            if(data.reload_timer>0){
                data.reload_timer--;
                if(data.reload_timer==0) SpeedCalc.reloadSpeed(data,speed);
            }
            if(data.train.derailed){
                speed=0.0;
                SpeedCalc.reloadSpeed(data,speed);
            }
        }
        return speed;
    }
    public static void dataResolver(TrainData data,double speed,double near_factor,
                                    double far_factor,boolean is_valid,boolean is_move){
        SpeedCalc.smooth(data,speed);
        data.near_amp=near_factor;
        data.far_amp=far_factor;
        data.is_last_valid=is_valid;
        data.is_last_move=is_move;
        data.is_mute=far_factor<1e-2;
    }
    public static void tick(Player player){
        if(player==null) return;
        Level level=player.level();
        Vec3 player_pos=player.position();
        for(TrainData data: trains){
            double near_factor=0.0,far_factor=0.0,avg_speed=0.0;
            int carriage_cnt=0;
            for(Carriage carriage:data.train.carriages){
                Tuple<Vec3,CarriageContraptionEntity> pos_info=PosHandler.getCarriageInf(carriage,level);
                if(pos_info==null) continue;
                Vec3 train_pos=pos_info.getA();
                double distance=train_pos.distanceTo(player_pos);
                near_factor+=Math.max(0.0,1.0-distance/near_distance);
                far_factor+=Math.max(0.0,1.0-distance/far_distance);
                CarriageContraptionEntity entity=pos_info.getB();
                Tuple<Double,Double> speed_info=calcSpeed(entity);
                if(speed_info==null) continue;
                avg_speed+=speed_info.getA()*speed_info.getB()*20.0;
                carriage_cnt++;
            }
            double speed=speedResolver(data,avg_speed,carriage_cnt);
            boolean is_valid=carriage_cnt!=0,is_move=speed>1e-2;
            dataResolver(data,speed,near_factor,far_factor,is_valid,is_move);
            SoundEvent.internalPlay(data,level,player_pos,near_factor,is_move);
        }
    }
}