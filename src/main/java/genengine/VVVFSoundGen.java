package genengine;
import createvvvfsim.Configs;
import net.minecraft.util.Tuple;
import vvvfsimulator.data.vvvf.Analyze;
import vvvfsimulator.data.vvvf.Manager;
import vvvfsimulator.data.vvvf.Struct;
import vvvfsimulator.generation.audio.trainsound.Audio;
import vvvfsimulator.generation.audio.trainsound.AudioFilter.CppConvolutionFilter;
import vvvfsimulator.generation.audio.trainsound.AudioResourceManager;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.calculation.Common;
import vvvfsimulator.vvvf.model.Struct.Domain;
import vvvfsimulator.vvvf.model.Struct.PhaseState;
public class VVVFSoundGen extends SoundGen{
    private static final int conv_size=Configs.conv_size.get();
    private static volatile int first_gear;
    private static volatile int second_gear;
    private static volatile double vvvf_amp;
    private static volatile double max_speed_f;
    private static volatile double motor_db;
    private static volatile double gear_harmonic_db;
    private static volatile double dry_wet_ratio;
    private static volatile double line_train_ratio;
    private static volatile Struct vvvf_config;
    private static final vvvfsimulator.data.trainaudio.Struct train_config;
    private final Domain domain=new Domain(train_config.motorSpec);
    private final CppConvolutionFilter conv_filter;
    private final double[] dry_buffer=new double[buffer_size];
    private final double[] wet_buffer=new double[buffer_size];
    private volatile double target_f=0.0;
    private double current_f=0.0;
    static{
        Tuple<Integer,double[]> ir=AudioResourceManager.readResourceAudioFileSample(
                AudioResourceManager.SAMPLE_IR_PATH);
        train_config=new vvvfsimulator.data.trainaudio.Struct();
        train_config.impulseResponseSampleRate=sample_rate;
        train_config.impulseResponse=AudioResourceManager.resampleLinear(ir.getB(),ir.getA(),sample_rate);
    }
    public VVVFSoundGen(){
        conv_filter=new CppConvolutionFilter(conv_size,train_config.impulseResponse);
    }
    public void setF(double speed){
        target_f=speed*max_speed_f;
    }
    @Override
    public void mixTo(double[] mix_buffer){
        double f_step=(target_f-current_f)/buffer_size;
        double amp_step=(target_amp-current_amp)/buffer_size;
        Struct config=vvvf_config;
        for(int i=0;i<buffer_size;i++){
            double last_base_f=Math.max(current_f,0.0);
            current_f+=f_step;
            current_amp+=amp_step;
            double base_f=Math.max(current_f,0.0);
            if(last_base_f>1e-9 && base_f>1e-9) domain.multiplyBaseWaveTime(last_base_f/base_f);
            else if(base_f<=1e-9) domain.setBaseWaveTime(0.0);
            domain.setBraking(f_step<0);
            domain.setPowerOff(base_f<=1e-9);
            domain.setControlFrequency(base_f);
            domain.setBaseWaveAngleFrequency(MyMath.M_2PI*base_f);
            domain.addTime(sample_dt);
            domain.addBaseWaveTime(sample_dt);
            domain.getCarrierInstance().time+=sample_dt;
            Analyze.calculate(domain,config);
            PhaseState state=Common.calculatePhaseState(domain,0.0);
            double line=0.01*(state.u-state.v);
            double dry=Audio.calculateTrainSoundFromCurrentState(domain,train_config);
            dry_buffer[i]=line*line_train_ratio+dry*(1-line_train_ratio);
        }
        conv_filter.process(dry_buffer,0,wet_buffer,0,buffer_size);
        if(target_amp<1e-2 && current_amp<1e-2) return;
        for(int i=0;i<buffer_size;i++){
            double mix=dry_buffer[i]*4*dry_wet_ratio+wet_buffer[i]*(1-dry_wet_ratio);
            mix_buffer[i]+=mix*vvvf_amp*current_amp;
        }
    }
    public static void reloadYamlData(){
        vvvf_config=Manager.deepClone(Manager.current);
    }
    @Override
    public void reload(){
        first_gear=Configs.first_gear.get();
        second_gear=Configs.second_gear.get();
        vvvf_amp=Configs.vvvf_amp.get();
        max_speed_f=Configs.max_speed_f.get();
        motor_db=Configs.motor_db.get();
        gear_harmonic_db=Configs.gear_harmonic_db.get();
        dry_wet_ratio=Configs.dry_wet_ratio.get();
        line_train_ratio=Configs.line_train_ratio.get();
        train_config.setCalculatedGearHarmonic(first_gear,second_gear);
        train_config.motorVolumeDb=motor_db-gear_harmonic_db;
        train_config.totalVolumeDb=gear_harmonic_db;
        reloadYamlData();
    }
}