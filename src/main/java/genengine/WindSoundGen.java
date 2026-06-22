package genengine;
import createvvvfsim.Configs;
import java.util.concurrent.ThreadLocalRandom;
import org.jtransforms.fft.DoubleFFT_1D;
public class WindSoundGen extends SoundGen{
    private static final double pink_r0=Configs.pink_r0;
    private static final double wind_base_amp=Configs.wind_base_amp;
    private static final double wind_mod_f=Configs.wind_mod_f;
    private static final double wind_mod_depth=Configs.wind_mod_depth;
    private static final double bg_shear_base=Configs.bg_shear_base;
    private static final double bg_shear_range=Configs.bg_shear_range;
    private static final double bg_shear_rate=Configs.bg_shear_rate;
    private static final double hp_cutoff=Configs.hp_cutoff;
    private static final double bg_wind_amp=Configs.bg_wind_amp;
    private static final double main_cauchy_amp=Configs.main_cauchy_amp;
    private static final double main_center_f=Configs.main_center_f;
    private static final double main_cauchy_gamma=Configs.main_cauchy_gamma;
    private static final double main_mod_f=Configs.main_mod_f;
    private static final double main_mod_depth=Configs.main_mod_depth;
    private static final double main_wind_amp=Configs.main_wind_amp;
    private static final int table_size=Configs.buffer_size*Configs.table_ratio;
    private static final double[] main_wind=new double[table_size];
    private final PinkNoiseFilter pink_bg=new PinkNoiseFilter();
    private final OnePoleLPF bg_lpf=new OnePoleLPF(bg_shear_base);
    private final OnePoleHPF bg_hpf=new OnePoleHPF(hp_cutoff);
    private final RandomWalk bg_shear=new RandomWalk();
    private static ThreadLocalRandom tlr;
    private volatile double target_f=0.0;
    private double current_f=0.0;
    private double total_t=0.0;
    private int table_index=0;
    static{
        tlr=ThreadLocalRandom.current();
        for(int i=0;i<table_size;i++) main_wind[i]=tlr.nextGaussian();
        DoubleFFT_1D fft=new DoubleFFT_1D(table_size);
        fft.realForward(main_wind);
        double log_ratio0=Math.log(1e-6/main_center_f);
        double gain_0=1.0/(1.0+Math.pow(log_ratio0/main_cauchy_gamma,2));
        double log_ratio_nyq=Math.log(sample_rate/main_center_f/2.0);
        double gain_nyq=1.0/(1.0+Math.pow(log_ratio_nyq/main_cauchy_gamma,2));
        main_wind[0]*=gain_0;
        main_wind[1]*=gain_nyq;
        int num_bins=table_size/2;
        for(int i=1;i<num_bins;i++){
            double freq=(double)i/(table_size*sample_dt);
            double log_ratio=Math.log(freq/main_center_f);
            double gain=1.0/(1.0+Math.pow(log_ratio/main_cauchy_gamma,2));
            main_wind[2*i]*=gain;
            main_wind[2*i+1]*=gain;
        }
        fft.realInverse(main_wind,true);
        double peak=0;
        for(int i=0;i<table_size;i++){
            double abs_val=Math.abs(main_wind[i]);
            if(peak<abs_val) peak=abs_val;
        }
        double norm_factor=main_cauchy_amp/peak;
        for(int i=0;i<table_size;i++) main_wind[i]*=norm_factor;
    }
    private static class PinkNoiseFilter{
        private double state=0.0;
        public double process(double white){
            state=pink_r0*state+(1.0-pink_r0)*white;
            return state;
        }
    }
    private static class OnePoleLPF{
        private double alpha;
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
    private static class OnePoleHPF{
        private final double alpha;
        private double prev_x=0.0;
        private double prev_y=0.0;
        public OnePoleHPF(double cutoff){
            alpha=Math.exp(-2.0*Math.PI*cutoff/sample_rate);
        }
        public double process(double x){
            double y=alpha*(prev_y+x-prev_x);
            prev_x=x;
            prev_y=y;
            return y;
        }
    }
    private static class RandomWalk{
        private static final double sigma=bg_shear_rate*Math.sqrt(sample_dt);
        private double value=0.0;
        public double step(){
            value+=tlr.nextGaussian(0.0,sigma);
            if(value<-bg_shear_range) value=-2.0*bg_shear_range-value;
            if(value>bg_shear_range) value=2.0*bg_shear_range-value;
            return value;
        }
    }
    public void setF(double speed){
        target_f=speed;
    }
    private double bgFactor(){
        return bg_wind_amp*current_f*current_f;
    }
    private double mainFactor(){
        return main_wind_amp*Math.pow(Math.abs(current_f),2.5);
    }
    @Override
    public void mixTo(double[] mix_buffer){
        tlr=ThreadLocalRandom.current();
        double f_step=(target_f-current_f)/buffer_size;
        double amp_step=(target_amp-current_amp)/buffer_size;
        for(int i=0;i<buffer_size;i++){
            current_f+=f_step;
            current_amp+=amp_step;
            if(current_amp<1e-2 || current_f<1e-2) continue;
            bg_lpf.setCutoff(bg_shear_base+bg_shear.step());
            double bg_lfo=0.5+0.5*Math.sin(2.0*Math.PI*wind_mod_f*total_t);
            double current_pink_bg=pink_bg.process(tlr.nextGaussian()*0.5);
            double bg_amp=Math.min(0.5,wind_base_amp*(1.0+wind_mod_depth*bg_lfo));
            double bg_wind=bg_hpf.process(bg_lpf.process(current_pink_bg)*bg_amp);
            double main_lfo=0.5+0.5*Math.sin(2.0*Math.PI*main_mod_f*total_t);
            if(table_index==table_size) table_index=0;
            double current_main_wind=main_wind[table_index]*(1.0+main_mod_depth*main_lfo);
            mix_buffer[i]+=(bg_wind*bgFactor()+current_main_wind*mainFactor())*current_amp;
            total_t+=sample_dt;
            table_index++;
        }
    }
}