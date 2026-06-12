package soundphysics;
import createvvvfsim.EnvData;
import createvvvfsim.TrainData;
import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class Handler{
    public EnvData getEnv(Vec3 train_pos,Vec3 player_pos,Level level){
        return new EnvData();
    }
    public void handle(double[] mix_buffer,List<TrainData> train_datas){
        for(TrainData train_data:train_datas){
            train_data.base_gen.mixTo(mix_buffer);
            train_data.vvvf_gen.mixTo(mix_buffer);
            train_data.wind_gen.mixTo(mix_buffer);
        }
    }
}