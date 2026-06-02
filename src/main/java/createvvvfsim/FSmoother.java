package createvvvfsim;
import java.util.Arrays;
public class FSmoother{
    private static final double max_speed=Configs.max_speed;
    private static final double max_acc=Configs.max_acc;
    private static final int speeds_length=Configs.speeds_length;
    private int speeds_index=0;
    private final double[] speed_samples=new double[speeds_length];
    private double last_speed=0.0;
    public double smoothF(double speed){
        speed_samples[speeds_index]=Math.min(speed,max_speed);
        speeds_index=(speeds_index+1)%speeds_length;
        double[] speeds=Arrays.copyOf(speed_samples,speeds_length);
        Arrays.sort(speeds);
        double med_speed=speeds[speeds_length/2];
        double delta=Math.clamp(med_speed-last_speed,-max_acc,max_acc);
        last_speed+=delta;
        return Math.clamp(last_speed/max_speed,0.0,1.0);
    }
    public void reloadF(double speed){
        Arrays.fill(speed_samples,speed);
        last_speed=speed;
    }
}