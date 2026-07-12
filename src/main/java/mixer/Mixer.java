package mixer;
import createvvvfsim.EnvData;
import createvvvfsim.TrainData;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import utils.Reloadable;
public class Mixer implements Reloadable{
    public EnvData getEnv(Level level,Player player,Vec3 train_pos){
        return new EnvData();
    }
    public void handle(double[] mix_buffer,List<TrainData> train_datas){
        for(TrainData train_data:train_datas){
            train_data.base_gen.mixTo(mix_buffer);
            train_data.vvvf_gen.mixTo(mix_buffer);
            train_data.wind_gen.mixTo(mix_buffer);
        }
    }
    @Override
    public void reload(){}
}