package createvvvfsim;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.Minecraft;
import soundphysics.Handler;
import soundphysics.remastered.HandlerPhysics;
public class Configs{
    //register
    public static final String mod_id="create_vvvf_simulator";
    public static final String version="1.0.0";
    public static final String sync_model_name="train_sync";
    public static final int mixin_priority=1027;
    public static final int tick_period=3;
    public static final Minecraft mc=Minecraft.getInstance();
    public static final Handler sound_handler;
    static{
        if(HandlerPhysics.register()) sound_handler=new HandlerPhysics();
        else sound_handler=new Handler();
    }
    //command
    public static final String command_vvvf="vvvf";
    public static final String command_reload="reload";
    public static final String command_return="§a[Create: VVVF-Simulator] Reloaded!§r";
    //audio
    public static final int sample_rate=44100;
    public static final int buffer_size=1<<12;
    public static final AudioFormat format=new AudioFormat(sample_rate,16,1,true,false);
    public static final double base_max_amp=0.02;
    public static final double base_current_f=120.0;
    public static final double near_distance=32.0;
    public static final double far_distance=96.0;
    //speed
    public static final double max_acc_ratio=1.05;
    public static final int speeds_length=5;
    //vvvf
    public static final double max_base_f=115.0;
    public static final int conv_block_size=512;
}