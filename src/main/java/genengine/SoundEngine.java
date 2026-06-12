package genengine;
import createvvvfsim.Configs;
import createvvvfsim.TrainData;
import createvvvfsim.TrainStatus;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import soundphysics.Handler;
public class SoundEngine{
    private static final int buffer_size=Configs.buffer_size;
    private static final AudioFormat format=Configs.format;
    private static final double[] mix_buffer=new double[buffer_size];
    private static final byte[] out_buffer=new byte[buffer_size*2];
    private static final Thread thread=new Thread(SoundEngine::mixLoop);
    private static final Handler handler=Configs.handler;
    private static final double main_amp=Configs.main_amp;
    private static volatile double settings_amp=0.0;
    private static double current_amp=0.0;
    private static SourceDataLine dataline;
    static{
        try{
            dataline=AudioSystem.getSourceDataLine(format);
            dataline.open(format,buffer_size*4);
            dataline.start();
        }
        catch(LineUnavailableException ignored){}
        thread.setDaemon(true);
        thread.start();
    }
    public static void setAmp(double volume){
        settings_amp=volume;
    }
    private static void mixLoop(){
        while(true){
            Arrays.fill(mix_buffer,0.0);
            List<TrainData> train_datas=TrainStatus.getTrainData();
            handler.handle(mix_buffer,train_datas);
            double amp_step=(settings_amp*main_amp-current_amp)/buffer_size;
            for(int i=0;i<buffer_size;i++){
                current_amp+=amp_step;
                double clipped=Math.clamp(mix_buffer[i],-1.0,1.0)*current_amp;
                short sample=(short)(clipped*Short.MAX_VALUE);
                out_buffer[i*2]=(byte)(sample&0xFF);
                out_buffer[i*2+1]=(byte)((sample>>8)&0xFF);
            }
            dataline.write(out_buffer,0,buffer_size*2);
        }
    }
}