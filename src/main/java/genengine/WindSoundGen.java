package genengine;
import java.util.concurrent.ThreadLocalRandom;
import org.jtransforms.fft.DoubleFFT_1D;
public class WindSoundGen extends SoundGen{
    //background sound config
    private static final double wind_base_amp=0.3;
    private static final double wind_mod_f=0.2;
    private static final double wind_mod_depth=0.4;
    private static final double bg_shear_base=800.0;
    private static final double bg_shear_range=400.0;
    private static final double bg_shear_rate=0.8;
    private static final double hp_cutoff=250.0;
    //main wind sound config
    private static final double main_wind_amp=0.65;
    private static final double main_center_f=700.0;
    private static final double main_cauchy_gamma=0.06;
    private static final double main_mod_f=0.15;
    private static final double main_mod_depth=0.3;
    //other sound config
    private static final double pink_r0=0.99;
    private static final int table_size=buffer_size*32;

    private final double[] main_wind=new double[table_size];
    private final PinkNoiseFilter pink_bg=new PinkNoiseFilter();
    private final OnePoleLPF bg_lpf=new OnePoleLPF(bg_shear_base);
    private final OnePoleHPF bg_hpf=new OnePoleHPF(hp_cutoff);
    private final RandomWalk bg_shear=new RandomWalk();
    private ThreadLocalRandom tlr;

    private volatile double target_f=0.0;
    private double current_f=0.0;
    private double total_t=0.0;
    private int table_index=0;

    public WindSoundGen(){
        tlr=ThreadLocalRandom.current();
        for(int i=0;i<table_size;i++) main_wind[i]=tlr.nextGaussian();
        DoubleFFT_1D fft=new DoubleFFT_1D(table_size);
        fft.realForward(main_wind);
        double log_ratio0=Math.log(1e-6/main_center_f);
        double gain0=1.0/(1.0+Math.pow(log_ratio0/main_cauchy_gamma,2));
        main_wind[0]*=gain0;
        double nyquist_freq=sample_rate/2.0;
        double log_ratio_nyq=Math.log(nyquist_freq/main_center_f);
        double gain_nyq=1.0/(1.0+Math.pow(log_ratio_nyq/main_cauchy_gamma,2));
        main_wind[1]*=gain_nyq;
        int num_bins=table_size/2;
        for(int k=1;k<num_bins;k++){
            double freq=(double)k/(table_size*sample_dt);
            double log_ratio=Math.log(freq/main_center_f);
            double gain=1.0/(1.0+Math.pow(log_ratio/main_cauchy_gamma,2));
            main_wind[2*k]*=gain;
            main_wind[2*k+1]*=gain;
        }
        fft.realInverse(main_wind,true);
        double peak=0;
        for(int i=0;i<table_size;i++){
            double abs_val=Math.abs(main_wind[i]);
            if(peak<abs_val) peak=abs_val;
        }
        double norm_factor=main_wind_amp/peak;
        for(int i=0;i<table_size;i++) main_wind[i]*=norm_factor;
    }

    private class PinkNoiseFilter{
        private double state=0.0;
        public double process(double white){
            state=pink_r0*state+(1.0-pink_r0)*white;
            return state;
        }
    }
    private class OnePoleLPF{
        private double alpha=0.0;
        private double y=0.0;
        public OnePoleLPF(double cutoff){
            setCutoff(cutoff);
        }
        public void setCutoff(double cutoff){
            alpha=Math.exp(-2.0*Math.PI*cutoff/sample_rate);
        }
        public double process(double x){
            y=x*(1.0-alpha)+alpha*y;
            return y;
        }
    }
    private class OnePoleHPF{
        private double alpha=0.0;
        private double y=0.0;
        private double prev_x=0.0;
        private double prev_y=0.0;
        public OnePoleHPF(double cutoff){
            alpha=Math.exp(-2.0*Math.PI*cutoff/sample_rate);
        }
        public double process(double x){
            y=alpha*(prev_y+x-prev_x);
            prev_x=x;
            prev_y=y;
            return y;
        }
    }
    private class RandomWalk{
        private static final double sigma=bg_shear_rate*Math.sqrt(sample_dt);
        private double value=0.0;
        public double step(){
            double delta=tlr.nextGaussian(0.0,sigma);
            value+=delta;
            if(value<-bg_shear_range) value=-2.0*bg_shear_range-value;
            if(value>bg_shear_range) value=2.0*bg_shear_range-value;
            return value;
        }
    }
    public void setF(double speed){
        target_f=speed;
    }
    private double bgFactor(){
        return 10.0*target_f*target_amp;
    }
    private double mainFactor(){
        return 0.03*target_f;
    }
    @Override
    public void mixTo(double[] mix_buffer){
        tlr=ThreadLocalRandom.current();
        double f_step=(target_f-current_f)/buffer_size;
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            current_f+=f_step;
            current_amp+=amp_step;
            double bg_offset=bg_shear.step();
            double bg_cutoff=Math.clamp(bg_shear_base+bg_offset,400.0,1200.0);
            bg_lpf.setCutoff(bg_cutoff);
            double white_bg=tlr.nextGaussian()*0.5;
            double current_pink_bg=pink_bg.process(white_bg);
            double bg_lfo=0.5+0.5*Math.sin(2.0*Math.PI*wind_mod_f*total_t);
            double bg_amp=Math.min(0.5,wind_base_amp*(1.0+wind_mod_depth*bg_lfo));
            double bg_lp=bg_lpf.process(current_pink_bg)*bg_amp;
            double bg_wind=bg_hpf.process(bg_lp);
            double main_lfo=0.5+0.5*Math.sin(2.0*Math.PI*main_mod_f*total_t);
            double main_amp=1.0+main_mod_depth*main_lfo;
            if(table_index==table_size) table_index=0;
            double current_main_wind=main_wind[table_index]*main_amp;
            mix_buffer[i]+=(bg_wind+current_main_wind*mainFactor())*bgFactor();
            total_t+=sample_dt;
            table_index++;
        }
    }
}