package genengine;
import createvvvfsim.Configs;
import java.util.concurrent.ThreadLocalRandom;
import org.jtransforms.fft.DoubleFFT_1D;
import signal.Highpass;
import signal.Lowpass;
import signal.RandomWalk;
public class WindSoundGen extends SoundGen{
    private static final int table_size=Configs.table_size.get();
    private static volatile double wind_base_amp;
    private static volatile double wind_mod_f;
    private static volatile double wind_mod_depth;
    private static volatile double bg_shear_base;
    private static volatile double bg_shear_range;
    private static volatile double bg_shear_rate;
    private static volatile double bg_shear_sigma;
    private static volatile double hp_cutoff;
    private static volatile double bg_wind_amp;
    private static volatile double main_cauchy_amp;
    private static volatile double main_center_f;
    private static volatile double main_cauchy_gamma;
    private static volatile double main_mod_f;
    private static volatile double main_mod_depth;
    private static volatile double main_wind_amp;
    private static volatile double pink_alpha;
    private static volatile double highpass_alpha;
    private static volatile double[] main_wind,temp;
    private final Lowpass pink_bg=new Lowpass();
    private final Lowpass bg_lpf=new Lowpass();
    private final Highpass bg_hpf=new Highpass();
    private final RandomWalk bg_shear=new RandomWalk();
    private static ThreadLocalRandom tlr;
    private volatile double target_f=0.0;
    private double current_f=0.0;
    private double total_t=0.0;
    private int table_index=0;
    public void setF(double speed){
        target_f=speed;
    }
    private double bgFactor(){
        return bg_wind_amp*current_f;
    }
    private double mainFactor(){
        return main_wind_amp*Math.pow(current_f,1.5);
    }
    @Override
    public void mixTo(double[] mix_buffer){
        tlr=ThreadLocalRandom.current();
        double f_step=(target_f-current_f)/buffer_size;
        double amp_step=(target_amp-current_amp)/buffer_size;
        if(target_amp<1e-2 && current_amp<1e-2){
            current_f=target_f;
            return;
        }
        if(target_f<1e-2 && current_f<1e-2){
            current_amp=target_amp;
            return;
        }
        for(int i=0;i<buffer_size;i++){
            current_f+=f_step;
            current_amp+=amp_step;
            current_f=Math.max(current_f,0);
            double bg_shear_value=bg_shear.step(bg_shear_sigma,bg_shear_range);
            double lowpass_alpha=1.0-Math.exp(-2.0*Math.PI*(bg_shear_base+bg_shear_value)/sample_rate);
            double bg_lfo=0.5+0.5*Math.sin(2.0*Math.PI*wind_mod_f*total_t);
            double current_pink_bg=pink_bg.process(pink_alpha,tlr.nextGaussian()*0.5);
            double bg_amp=Math.min(0.5,wind_base_amp*(1.0+wind_mod_depth*bg_lfo));
            double lowpass_value=bg_lpf.process(lowpass_alpha,current_pink_bg);
            double bg_wind=bg_hpf.process(highpass_alpha,lowpass_value*bg_amp);
            double main_lfo=0.5+0.5*Math.sin(2.0*Math.PI*main_mod_f*total_t);
            if(table_index==table_size) table_index=0;
            double current_main_wind=main_wind[table_index]*(1.0+main_mod_depth*main_lfo);
            mix_buffer[i]+=(bg_wind*bgFactor()+current_main_wind*mainFactor())*current_amp;
            total_t+=sample_dt;
            table_index++;
        }
    }
    @Override
    public void reload(){
        wind_base_amp=Configs.wind_base_amp.get();
        wind_mod_f=Configs.wind_mod_f.get();
        wind_mod_depth=Configs.wind_mod_depth.get();
        bg_shear_base=Configs.bg_shear_base.get();
        bg_shear_range=Configs.bg_shear_range.get();
        bg_shear_rate=Configs.bg_shear_rate.get();
        bg_shear_sigma=bg_shear_rate*Math.sqrt(sample_dt);
        hp_cutoff=Configs.hp_cutoff.get();
        bg_wind_amp=Configs.bg_wind_amp.get();
        main_cauchy_amp=Configs.main_cauchy_amp.get();
        main_center_f=Configs.main_center_f.get();
        main_cauchy_gamma=Configs.main_cauchy_gamma.get();
        main_mod_f=Configs.main_mod_f.get();
        main_mod_depth=Configs.main_mod_depth.get();
        main_wind_amp=Configs.main_wind_amp.get();
        pink_alpha=1.0-Configs.pink_r0.get();
        highpass_alpha=Math.exp(-2.0*Math.PI*hp_cutoff/sample_rate);
        temp=new double[table_size];
        tlr=ThreadLocalRandom.current();
        for(int i=0;i<table_size;i++) temp[i]=tlr.nextGaussian();
        DoubleFFT_1D fft=new DoubleFFT_1D(table_size);
        fft.realForward(temp);
        double log_ratio0=Math.log(1e-6/main_center_f);
        double gain_0=1.0/(1.0+Math.pow(log_ratio0/main_cauchy_gamma,2));
        double log_ratio_nyq=Math.log(sample_rate/main_center_f/2.0);
        double gain_nyq=1.0/(1.0+Math.pow(log_ratio_nyq/main_cauchy_gamma,2));
        temp[0]*=gain_0;
        temp[1]*=gain_nyq;
        int num_bins=table_size/2;
        for(int i=1;i<num_bins;i++){
            double freq=(double)i/(table_size*sample_dt);
            double log_ratio=Math.log(freq/main_center_f);
            double gain=1.0/(1.0+Math.pow(log_ratio/main_cauchy_gamma,2));
            temp[2*i]*=gain;
            temp[2*i+1]*=gain;
        }
        fft.realInverse(temp,true);
        double peak=0;
        for(int i=0;i<table_size;i++){
            double abs_val=Math.abs(temp[i]);
            if(peak<abs_val) peak=abs_val;
        }
        double norm_factor=main_cauchy_amp/peak;
        for(int i=0;i<table_size;i++) temp[i]*=norm_factor;
        main_wind=temp;
    }
}