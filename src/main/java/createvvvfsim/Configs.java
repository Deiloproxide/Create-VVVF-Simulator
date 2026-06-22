package createvvvfsim;
public class Configs{
    //register
    public static final String mod_id="create_vvvf_simulator";
    public static final String sync_name="train_sync";
    public static final String sound_name="train_vvvf";
    public static final String version="1.0.0";
    public static final int mixin_priority=1027;
    public static final int sync_period=3;
    public static final int eval_period=3;
    //command
    public static final String command_vvvf="vvvf";
    public static final String command_reload="reload";
    public static final String command_return="§a[Create: VVVF-Simulator] Reloaded!§r";
    //resource location
    public static final String filter_wav="/assets/createvvvfsim/TrainSound/Filter.wav";
    public static final String table="/assets/createvvvfsim/SwitchAngle/";
    //audio
    public static final int sample_rate=44100;
    public static final int buffer_size=1<<12;
    //main amp
    public static final double main_amp=1.5;
    //train start
    public static final double gas_amp=0.6;
    public static final double switch_amp=0.4;
    //base
    public static final double base_current_f=120.0;
    public static final double brown_sigma=0.05;
    public static final double brown_range=2.0;
    //base amp
    public static final double base_amp=0.04;
    public static final double brown_amp=0.02;
    //vvvf
    public static final double max_base_f=115.0;
    public static final int conv_block_size=512;
    //vvvf amp
    public static final double vvvf_amp=0.5;
    //background wind
    public static final double pink_r0=0.99;
    public static final double wind_base_amp=0.3;
    public static final double wind_mod_f=0.2;
    public static final double wind_mod_depth=0.4;
    public static final double bg_shear_base=800.0;
    public static final double bg_shear_range=400.0;
    public static final double bg_shear_rate=0.8;
    public static final double hp_cutoff=250.0;
    //main wind
    public static final double main_cauchy_amp=0.65;
    public static final double main_center_f=700.0;
    public static final double main_cauchy_gamma=0.05;
    public static final double main_mod_f=0.15;
    public static final double main_mod_depth=0.3;
    public static final int table_ratio=32;
    //wind amp
    public static final double bg_wind_amp=3.0;
    public static final double main_wind_amp=0.15;
    //sound spread
    public static final double near_distance=32.0;
    public static final double far_distance=96.0;
    //speed
    public static final double max_acc_ratio=1.05;
    public static final int speeds_length=5;
}