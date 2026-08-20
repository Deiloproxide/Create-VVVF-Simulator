package createvvvfsim.data;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.signal.CauchyNoise;
import createvvvfsim.signal.Highpass;
import createvvvfsim.signal.Lowpass;
import createvvvfsim.signal.RandomWalk;
import createvvvfsim.types.FromType;
import java.util.Arrays;
public class TrainData{
    private static final int speeds_length=SpecConfig.speeds_length.get();
    public final Train train;
    public final Lowpass pink_bg=new Lowpass();
    public final Lowpass bg_lpf=new Lowpass();
    public final Highpass bg_hpf=new Highpass();
    public final CauchyNoise noise=new CauchyNoise();
    public final RandomWalk bg_shear=new RandomWalk();
    public final RandomWalk brown=new RandomWalk();
    public final EnvData target=new EnvData();
    public final EnvData current=new EnvData();
    public final EnvData step=new EnvData();
    public final ConvData conv_data=new ConvData();
    public final Lowpass[] filters=new Lowpass[5];
    public volatile double dist_amp=0.0;
    public volatile double speed_per=0.0;
    public final double[] speed_samples=new double[speeds_length];
    public final double[] dist_amps={0.0,0.0,0.0};
    public final double[] speed_pers={0.0,0.0,0.0};
    public final int[] slots={0,0,0,0};
    public double last_speed=0.0,phase=0.0,total_t=0.0;
    public boolean use_server_speed=false;
    public boolean server_reloaded=false,is_mute=true;
    public boolean is_last_valid=false,is_last_move=false;
    public int speeds_index=0,reload_timer=0;
    public int config_from=FromType.server.ordinal();
    public TrainData(Train train){
        Arrays.setAll(filters,i->new signal.Lowpass());
        this.train=train;
    }
}