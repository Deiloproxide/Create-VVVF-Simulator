package createvvvfsim.reverber;
import createvvvfsim.data.EnvData;
import createvvvfsim.data.GlobalData;
import createvvvfsim.data.TrainData;
import createvvvfsim.generator.BaseSoundGen;
import createvvvfsim.generator.SoundGen;
import createvvvfsim.generator.VVVFSoundGen;
import createvvvfsim.generator.WindSoundGen;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class Reverber{
    protected static final SoundGen[] gens={new BaseSoundGen(),new VVVFSoundGen(),new WindSoundGen()};
    public boolean register(){
        return true;
    }
    public EnvData getEnv(Level level,Player player,Vec3 train_pos){
        return new EnvData();
    }
    public void handle(double[] mix_buffer){
        List<TrainData> datas=GlobalData.getAllTrains();
        for(TrainData data:datas)
            for(SoundGen gen:gens)
                gen.mixTo(data,mix_buffer);
    }
}