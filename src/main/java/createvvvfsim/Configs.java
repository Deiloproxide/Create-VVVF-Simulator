package createvvvfsim;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CTrains;
import net.minecraft.client.Minecraft;
import javax.sound.sampled.AudioFormat;
public class Configs{
    //basic
    public static final int mixin_priority=1027;
    //audio
    public static final int sample_rate=44100;
    public static final int buffer_size=1<<12;
    public static final AudioFormat format=new AudioFormat(sample_rate,16,1,true,false);
    public static final double sample_dt=1.0/sample_rate;
    public static final double base_max_amp=0.05;
    public static final double base_current_f=120.0;
    public static final double max_distance=32;
    //speed
    public static final CTrains train_config=AllConfigs.server().trains;
    public static final double max_speed=train_config.trainTopSpeed.getF();
    public static final double max_acc=train_config.trainAcceleration.getF()*1.01/20.0;
    public static final int speeds_length=7;
    //vvvf
    public static final double max_base_f=115.0;
    public static final int conv_block_size=512;
}