package createvvvfsim;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CTrains;
import net.minecraft.world.phys.Vec3;
import java.util.Arrays;
import vvvf.calculation.Common;
import vvvf.model.Motor;
import vvvf.model.Struct;
import vvvf.model.Struct.ElectricalParameter.CarrierParameter;
public class VVVFSoundGen{
    //sound config
    private static final int sample_rate=VVVFSoundEngine.sample_rate;
    private static final int buffer_size=VVVFSoundEngine.buffer_size;
    private static final double sample_dt=1.0/sample_rate;
    private static final float max_amp=0.1f;
    private static final float max_distance=64f;
    //train config
    private static final CTrains trains_config=AllConfigs.server().trains;
    private static final float max_speed=trains_config.trainTopSpeed.getF();
    private static final float max_acc=trains_config.trainAcceleration.getF()/400f;
    private static final float max_control_f=115f;
    //speed filter config
    private static final int speeds_length=8;

    private int speeds_index=0;
    private final float[] speed_samples=new float[speeds_length];
    private float last_speed=0f;

    private volatile float target_f=0f;
    private volatile float target_amp=0f;
    private float current_f=0f;
    private float current_amp=0f;

    private final Struct.Domain domain=new Struct.Domain(new Motor.MotorSpecification());
    private final Struct.PulseControl pulse_control=new Struct.PulseControl();
    private final CarrierParameter.RandomFrequency carrier_random_f=new CarrierParameter.RandomFrequency(0.0,1.0);
    private final CarrierParameter.ConstantFrequency carrier_main_f=new CarrierParameter.ConstantFrequency(240.0);
    private final CarrierParameter carrier_f=new CarrierParameter(carrier_random_f,carrier_main_f);
    private final Struct.ElectricalParameter elect_state=new Struct.ElectricalParameter(false,false,2,pulse_control,carrier_f,null,0.0,0.0);
    public VVVFSoundGen(){
        //pulse_control.pulseMode.baseWave=Struct.PulseControl.Pulse.BaseWaveType.Saw;
        domain.electricalState=elect_state;
    }
    public void updateF(Vec3 move){
        //避免客户端与服务器连接不稳定造成的速度波动
        float raw_speed=(float)move.length()/0.05f;
        raw_speed=Math.min(raw_speed,max_speed);
        speed_samples[speeds_index]=raw_speed;
        speeds_index=(speeds_index+1)%speeds_length;
        float[] speeds=Arrays.copyOf(speed_samples,speeds_length);
        Arrays.sort(speeds);
        float med_speed=speeds[speeds_length/2];
        float max_speed_delta=max_acc*20f;
        float delta=Math.clamp(med_speed-last_speed,-max_speed_delta,max_speed_delta);
        last_speed+=delta;
        target_f=Math.clamp(last_speed/max_speed,0f,1f)*max_control_f;
    }
    public void updateAmp(Vec3 train_pos,Vec3 player_pos){
        float distance=(float)train_pos.distanceTo(player_pos);
        target_amp=max_amp*(distance<max_distance?1f-distance/max_distance:0f);
    }
    public void mixTo(float[] mix_buffer){
        float f_step=(target_f-current_f)/buffer_size;
        float amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            double last_base_f=Math.max(current_f,0f);
            current_f+=f_step;
            current_amp+=amp_step;
            double base_f=Math.max(current_f,0f);
            Struct.PulseControl.Pulse.PulseTypeName pulse_type;
            int pulse_count;


            //策略1：南车西门子Siemens
            Struct.PulseControl.Pulse.PulseAlternative pulse_alt;
            if(base_f<18f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
                carrier_main_f.value=14.0*base_f+240.0;
            }//异步240Hz-450Hz
            else if(18f<=base_f && base_f<38.4f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
                carrier_main_f.value=450.0;
            }//异步450Hz
            else if(38.4f<=base_f && base_f<45.6f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=9;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Alt6;
            }//CHM9 Alt6
            else if(45.6f<=base_f && base_f<48f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=9;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
            }//CHM9 默认
            else if(48f<=base_f && base_f<50.4f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=7;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Alt3;
            }//CHM7 Alt3
            else if(50.4f<=base_f && base_f<54f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=7;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
            }//CHM7 默认
            else if(54f<=base_f && base_f<62.4f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=7;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Alt3;
            }//CHM7 Alt3
            else if(62.4f<=base_f && base_f<64f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=5;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Alt3;
            }//CHM5 Alt3
            else{
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=1;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
            }//同步方波
            elect_state.baseWaveFrequency=base_f;
            elect_state.baseWaveAmplitude=base_f/65.0;
            pulse_control.pulseMode.alternative=pulse_alt;
            /*
            //策略2：阿尔斯通Alstom
            if(base_f<13.5f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                carrier_main_f.value=300;
            }//异步300Hz
            else if(13.5f<=base_f && base_f<27f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                carrier_main_f.value=400;
            }//异步400Hz
            else if(27f<=base_f && base_f<42f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=21;
            }//同步21分频
            else if(42f<=base_f && base_f<57f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=15;
            }//同步15分频
            else if(57<=base_f && base_f<90f){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=9;
            }//同步9分频
            else{
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=1;
            }//同步方波
            elect_state.baseWaveFrequency=base_f;
            elect_state.baseWaveAmplitude=0.013178*base_f-0.011358;
            */
            elect_state.isZeroOutput=base_f<=0.0;
            pulse_control.pulseMode.pulseType=pulse_type;
            pulse_control.pulseMode.pulseCount=pulse_count;
            if(last_base_f>1e-9 && base_f>1e-9) domain.multiplyBaseWaveTime(last_base_f/base_f);
            else if(base_f<=1e-9) domain.setBaseWaveTime(0.0);
            domain.addTime(sample_dt);
            domain.addBaseWaveTime(sample_dt);
            domain.getCarrierInstance().time+=sample_dt;
            Struct.PhaseState state=Common.getCalculator(2,pulse_type).calculate(domain,0.0);
            domain.motor.process(domain.getDeltaTime(),elect_state.getBaseWaveAngleFrequency(),state);
            mix_buffer[i]+=((state.u-state.v)*0.5f)*current_amp;
        }
    }
}