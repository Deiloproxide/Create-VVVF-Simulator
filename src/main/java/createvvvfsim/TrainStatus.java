package createvvvfsim;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class TrainStatus{
    private static final double max_distance=Configs.max_distance;
    public static final List<TrainStatus> all_trains=new ArrayList<>();
    public final Train train;
    public final SoundGen gen=new SoundGen();
    public final VVVFSoundGen vvvf_gen=new VVVFSoundGen();
    private final FSmoother f_smoother=new FSmoother();
    private boolean is_move=false,is_last_move=false;
    public static final Object train_lock=new Object();


    private int cnt=0;
    private TrainStatus(Train train){
        this.train=train;
    }
    public static void addTrain(Train train){
        TrainStatus status=new TrainStatus(train);
        synchronized(train_lock){
            all_trains.add(status);
        }
    }
    public static void tick(Level level,Player player){
        synchronized(train_lock){
            all_trains.removeIf(status->status.isInvalid(level));
            for(TrainStatus status:all_trains){
                status.is_move=Math.abs(status.train.speed)>1e-2;
                Vec3 player_pos=player.position();
                double total_factor=0.0;
                for(Carriage carriage:status.train.carriages){
                    Vec3 train_pos=getPos(carriage,level);
                    if(train_pos==null) continue;
                    double distance=train_pos.distanceTo(player_pos);
                    total_factor+=Math.max(0.0,1.0-distance/max_distance);
                }
                if(status.is_move && !status.is_last_move){
                    level.playLocalSound(player,SoundEvents.LAVA_EXTINGUISH,
                            SoundSource.NEUTRAL,0.75f*(float)total_factor,1f);
                    level.playLocalSound(player,SoundEvents.WOODEN_TRAPDOOR_CLOSE,
                            SoundSource.NEUTRAL,0.6f*(float)total_factor,1.5f);
                }
                status.gen.setAmp(total_factor);
                status.vvvf_gen.setAmp(total_factor);

                double original=Math.abs(status.train.speed)*20.0;
                double smoothed=status.f_smoother.smoothF(original);
                status.cnt++;
                if(status.cnt==5){
                    status.cnt=0;
                    System.out.printf("Original: %f, Smoothed: %f\n",original/Configs.max_speed,smoothed);
                }
                status.vvvf_gen.setF(smoothed);
                //status.vvvf_gen.setF(original/Configs.max_speed);
                status.is_last_move=status.is_move;
            }
        }
    }
    private static Vec3 getPos(Carriage carriage,Level level){
        var dce=carriage.getDimensionalIfPresent(level.dimension());
        if(dce==null) return null;
        CarriageContraptionEntity entity=dce.entity.get();
        if(entity!=null) return entity.position();
        if(dce.positionAnchor!=null) return dce.positionAnchor;
        Vec3 lead=dce.leadingAnchor(),trail=dce.trailingAnchor();
        if(lead!=null && trail!=null) return lead.add(trail).scale(0.5);
        return lead!=null?lead:trail;
    }
    private boolean isInvalid(Level level){
        if(level==null) return true;
        if(this.train.id==null) return true;
        if(this.train.carriages==null || this.train.carriages.isEmpty()) return true;
        var railways=Create.RAILWAYS.sided(level);
        if(railways==null || railways.trains==null) return true;
        return !railways.trains.containsKey(this.train.id);
    }
}