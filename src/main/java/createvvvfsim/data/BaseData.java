package createvvvfsim.data;
public class BaseData{
    public class ModData{
        public double f=0.0,depth=0.0;
    }
    public class WalkData{
        public double mu=0.0,sigma=1.0,range=1.0;
    }
    public double begin_amp=0.0;
    public double base_amp=0.0;
    public double brown_amp=0.0;
    public double bg_amp=0.0;
    public double bg_wind_amp=0.0;
    public double main_wind_amp=0.0;
    public double base_f=0.0;
    public double cutoff_f=0.0;
    public double center_f=0.0;
    public double cauchy_gamma=1.0;
    public ModData bg=new ModData();
    public ModData main=new ModData();
    public WalkData brown=new WalkData();
    public WalkData pink=new WalkData();
    public WalkData shear=new WalkData();
    public double[] base_amps={0.0,0.0,0.0,0.0};
    public double[] base_phases={0.0,0.0,0.0,0.0};
}