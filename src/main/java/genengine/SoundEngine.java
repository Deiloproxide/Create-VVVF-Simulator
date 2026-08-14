package genengine;
import createvvvfsim.Configs;
import createvvvfsim.TrainData;
import createvvvfsim.TrainStatus;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import utils.ALlib;
import utils.IReloadable;
/**client class*/
public class SoundEngine implements IReloadable{
    private static final int buffer_size=Configs.buffer_size.get();
    private static final int buffer_cnt=Configs.buffer_cnt.get();
    private static final double[] mix_buffer=new double[buffer_size];
    private static final ByteBuffer[] out_buffer=new ByteBuffer[buffer_cnt];
    private static final Thread thread=new Thread(SoundEngine::mixLoop);
    private static final Object buffer_lock=new Object();
    private static volatile boolean is_run=false,is_paused=false;
    private static volatile double main_amp;
    private static volatile int buffer_remain=4;
    private static double current_amp=0.0;
    private static int buffer_ptr=0;
    static{
        Arrays.setAll(out_buffer,i->ByteBuffer.allocateDirect(buffer_size*4));
        for(ByteBuffer buffer:out_buffer) buffer.order(ByteOrder.LITTLE_ENDIAN);
        thread.setDaemon(true);
        thread.start();
    }
    public static void load(){
        is_run=false;
        ALlib.disable();
        ALlib.load();
        synchronized(buffer_lock){
            buffer_remain=4;
        }
        is_run=true;
        ALlib.enable();
        ALlib.clear();
    }
    public static void mixTask(){
        if(!is_run) return;
        synchronized(buffer_lock){
            buffer_remain--;
            buffer_lock.notify();
        }
    }
    public static void setPause(boolean paused){
        is_paused=paused;
        if(is_paused) ALlib.pause();
        else ALlib.resume();
    }
    private static void mixLoop(){
        while(true){
            Arrays.fill(mix_buffer,0.0);
            out_buffer[buffer_ptr].clear();
            List<TrainData> train_datas=TrainStatus.getTrainData();
            TrainData.mixer.handle(mix_buffer,train_datas);
            double amp_step=(main_amp-current_amp)/buffer_size;
            for(int i=0;i<buffer_size;i++){
                current_amp+=amp_step;
                double clipped=Math.min(Math.max(mix_buffer[i],-1.0),1.0)*current_amp;
                short sample=(short)(clipped*Short.MAX_VALUE);
                out_buffer[buffer_ptr].putShort(sample);
                out_buffer[buffer_ptr].putShort(sample);
            }
            out_buffer[buffer_ptr].flip();
            synchronized(buffer_lock){
                if(buffer_remain==buffer_cnt){
                    try{
                        buffer_lock.wait();
                    }
                    catch(InterruptedException ignored){}
                }
                ALlib.feed(out_buffer[buffer_ptr]);
                buffer_ptr++;
                if(buffer_ptr==buffer_cnt) buffer_ptr=0;
                if(buffer_remain==0) ALlib.resume();
                buffer_remain++;
            }
        }
    }
    @Override
    public void reload(){
        main_amp=Configs.main_amp.get();
    }
}