package genengine;
import createvvvfsim.Configs;
import vvvfsimulator.generation.audio.trainsound.Audio;
import vvvfsimulator.generation.audio.trainsound.AudioFilter.CppConvolutionFilter;
import vvvfsimulator.generation.audio.trainsound.AudioResourceManager;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.calculation.Common;
import vvvfsimulator.vvvf.model.Struct;
import vvvfsimulator.vvvf.model.Struct.ElectricalParameter.CarrierParameter;
public class VVVFSoundGen extends SoundGen{
    private static final double max_base_f=Configs.max_base_f;
    private static final int conv_block_size=Configs.conv_block_size;
    private volatile double target_f=0.0;
    private double current_f=0.0;
    private final Struct.PulseControl pulse_control=new Struct.PulseControl();
    private final CarrierParameter.RandomFrequency carrier_random_f=new CarrierParameter.RandomFrequency(0.0,30.0);
    private final CarrierParameter.ConstantFrequency carrier_main_f=new CarrierParameter.ConstantFrequency(0.0);
    private final CarrierParameter carrier_f=new CarrierParameter(carrier_random_f,carrier_main_f);
    private final Struct.ElectricalParameter elect_state=new Struct.ElectricalParameter(false,false,2,pulse_control,carrier_f,null,0.0,0.0);
    private final vvvfsimulator.data.trainaudio.Struct train_config=new vvvfsimulator.data.trainaudio.Struct();
    private final CppConvolutionFilter conv_filter;
    private final Struct.Domain domain=new Struct.Domain(train_config.motorSpec);
    private final double[] dry_buffer=new double[buffer_size],wet_buffer=new double[buffer_size];
    public VVVFSoundGen(){
        int[] ir_sample_rate={-1};
        double[] ir=AudioResourceManager.readResourceAudioFileSample(AudioResourceManager.SAMPLE_IR_PATH,ir_sample_rate);
        train_config.impulseResponseSampleRate=sample_rate;
        train_config.impulseResponse=AudioResourceManager.resampleLinear(ir,ir_sample_rate[0],sample_rate);
        train_config.setCalculatedGearHarmonic(19,120);
        if(train_config.harmonicSound.isEmpty()) addDefaultMotorHarmonics(train_config);
        train_config.motorVolumeDb=0;
        train_config.totalVolumeDb=-2;
        conv_filter=new CppConvolutionFilter(conv_block_size,train_config.impulseResponse);
        domain.electricalState=elect_state;
    }
    private static void addDefaultMotorHarmonics(vvvfsimulator.data.trainaudio.Struct config){
        double[] harmonics={1.0,2.0,9.5},amps={0.3,0.3,1.2};
        for(int i=0;i<harmonics.length;i++){
            vvvfsimulator.data.trainaudio.Struct.HarmonicData h=new vvvfsimulator.data.trainaudio.Struct.HarmonicData();
            h.harmonic=harmonics[i];
            h.disappear=-1.0;
            h.range.start=0.0;
            h.range.end=-1.0;
            h.amplitude.start=0.0;
            h.amplitude.startValue=0.0;
            h.amplitude.end=40.0;
            //h.amplitude.endValue=0.16*Math.pow(0.62,i);
            h.amplitude.endValue=0.1*Math.pow(0.8,amps[i]);
            h.amplitude.minimumValue=0.0;
            h.amplitude.maximumValue=0.16;
            config.harmonicSound.add(h);
        }
    }
    public void setF(double speed){
        target_f=speed*max_base_f;
    }
    @Override
    public void mixTo(double[] mix_buffer){
        double f_step=(target_f-current_f)/buffer_size;
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            double last_base_f=Math.max(current_f,0.0);
            current_f+=f_step;
            current_amp+=amp_step;
            double base_f=Math.max(current_f,0.0);
            Struct.PulseControl.Pulse.PulseTypeName pulse_type;
            int pulse_count;

            //Strategy1: Siemens
            Struct.PulseControl.Pulse.PulseAlternative pulse_alt;
            if(base_f<3.0){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
                carrier_main_f.value=240.0;
            }//Start
            else if(base_f<18.0){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
                //carrier_main_f.value=14.0*base_f+198.0;
                carrier_main_f.value=240.0*Math.pow(1.875,(base_f-3.0)/15.0);
            }//Async 240Hz-450Hz
            else if(base_f<44.0){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
                carrier_main_f.value=450.0;
            }//Async 450Hz
            else if(base_f<58.5){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=9;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
            }//CHM9 Default
            else if(base_f<78.0){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=7;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
            }//CHM7 Default
            else if(base_f<95.0){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.CHM;
                pulse_count=5;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
            }//CHM5 Default
            else{
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=1;
                pulse_alt=Struct.PulseControl.Pulse.PulseAlternative.Default;
            }//Square
            elect_state.baseWaveFrequency=base_f;
            if(base_f<44.0) elect_state.baseWaveAmplitude=0.01626*base_f;
            else elect_state.baseWaveAmplitude=Math.min(0.0211*base_f-0.1657,1.245);
            domain.setBaseWaveAngleFrequency(MyMath.M_2PI*base_f);
            pulse_control.pulseMode.alternative=pulse_alt;
            /*
            //Strategy2: Alstom
            double base_wave_amp=0.0196*base_f;
            if(base_f<15){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                carrier_main_f.value=300;
            }//Async 300Hz
            else if(base_f<27){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.ASYNC;
                pulse_count=1;
                carrier_main_f.value=400;
            }//Async 400Hz
            else if(base_f<42){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=21;
            }//Sync 21
            else if(base_f<60){
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=15;
                base_wave_amp=0.04*base_f-0.88;
            }//Sync 15
            else{
                pulse_type=Struct.PulseControl.Pulse.PulseTypeName.SYNC;
                pulse_count=11;
                base_wave_amp=0.41*base_f-23.0;
            }//Sync 11
            elect_state.baseWaveFrequency=base_f;
            elect_state.baseWaveAmplitude=base_wave_amp;
            domain.setBaseWaveAngleFrequency(MyMath.M_2PI*base_f);
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
            domain.motor.process(domain.getDeltaTime(),MyMath.M_2PI*base_f,state);
            double train_sound=Audio.calculateTrainSoundFromCurrentState(domain,train_config);
            dry_buffer[i]=train_sound*current_amp;
        }
        conv_filter.process(dry_buffer,0,wet_buffer,0,buffer_size);
        for(int i=0;i<buffer_size;i++) mix_buffer[i]+=wet_buffer[i]*0.5;
    }
}