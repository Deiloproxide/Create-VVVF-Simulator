package createvvvfsim.calculator;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CTrains;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.data.TrainData;
import createvvvfsim.util.Instance;
import java.lang.reflect.Method;
import java.util.Arrays;
public class SpeedCalc{
    private static final CTrains train_config=AllConfigs.server().trains;
    private static final int speeds_length=SpecConfig.speeds_length.get();
    private static volatile double max_speed;
    private static volatile double max_acc;
    public static void smooth(TrainData data,double speed){
        double[] samples=data.speed_samples;
        samples[data.speeds_index]=Math.min(speed,max_speed);
        data.speeds_index=(data.speeds_index+1)%speeds_length;
        double[] sorted_samples=Arrays.copyOf(samples,speeds_length);
        Arrays.sort(sorted_samples);
        double med_speed=sorted_samples[speeds_length/2];
        double delta=Math.min(Math.max(med_speed-data.speed,-max_acc),max_acc);
        data.speed+=delta;
        data.speed_per=data.speed/max_speed;
    }
    public static void reloadSpeed(TrainData data,double speed){
        double filted_speed=Math.min(speed,max_speed);
        Arrays.fill(data.speed_samples,filted_speed);
        data.speed=filted_speed;
        data.speed_per=filted_speed/max_speed;
    }
    public static void loadCreate(){
        try{
            Instance ctrains=new Instance(train_config);
            Instance top_speed=ctrains.get("trainTopSpeed");
            Instance acc=ctrains.get("trainAcceleration");
            Method get_speed=top_speed.getMethod("getF");
            Method get_acc=acc.getMethod("getF");
            double max_acc_ratio=SpecConfig.max_acc_ratio.get();
            max_speed=top_speed.invoke(Float.class,get_speed);
            max_acc=acc.invoke(Float.class,get_acc)*max_acc_ratio/20.0;
        }
        catch(Exception ignored){}
    }
}