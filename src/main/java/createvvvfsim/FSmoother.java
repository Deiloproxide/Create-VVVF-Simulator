package createvvvfsim;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CTrains;
import java.lang.reflect.Method;
import java.util.Arrays;
import utils.Instance;
import utils.Reloadable;
public class FSmoother implements Reloadable{
    private static final CTrains train_config=AllConfigs.server().trains;
    private static final int speeds_length=Configs.speeds_length.get();
    private static volatile double max_speed;
    private static volatile double max_acc_ratio;
    private static volatile double max_acc;
    private int speeds_index=0;
    private final double[] speed_samples=new double[speeds_length];
    private double last_speed=0.0;
    public double smoothF(double speed){
        speed_samples[speeds_index]=Math.min(speed,max_speed);
        speeds_index=(speeds_index+1)%speeds_length;
        double[] speeds=Arrays.copyOf(speed_samples,speeds_length);
        Arrays.sort(speeds);
        double med_speed=speeds[speeds_length/2];
        double delta=Math.min(Math.max(med_speed-last_speed,-max_acc),max_acc);
        last_speed+=delta;
        return Math.min(Math.max(last_speed/max_speed,0.0),1.0);
    }
    public void reloadF(double speed){
        Arrays.fill(speed_samples,Math.min(speed,max_speed));
        last_speed=speed;
    }
    public static void reloadCreate(){
        try{
            Instance ctrains=new Instance(train_config);
            Instance top_speed=ctrains.get("trainTopSpeed");
            Instance acc=ctrains.get("trainAcceleration");
            Method get_speed=top_speed.getMethod("getF");
            Method get_acc=acc.getMethod("getF");
            max_speed=top_speed.invoke(Float.class,get_speed);
            max_acc=acc.invoke(Float.class,get_acc)*max_acc_ratio/20.0;
        }
        catch(Exception ignored){}
    }
    @Override
    public void reload(){
        max_acc_ratio=Configs.max_acc_ratio.get();
    }
}