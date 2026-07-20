package genengine;
import createvvvfsim.Configs;
import createvvvfsim.TrainData;
import createvvvfsim.TrainStatus;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import utils.ALlib;
import utils.Reloadable;
public class SoundEngine implements Reloadable{
    private static final int buffer_size=Configs.buffer_size.get();
    private static final int buffer_cnt=4;
    private static final double[] mix_buffer=new double[buffer_size];
    private static final ByteBuffer[] out_buffer=new ByteBuffer[buffer_cnt];
    private static final Thread thread=new Thread(SoundEngine::mixLoop);
    private static final Object mix_lock=new Object();
    private static volatile boolean is_run=false,is_paused=false;
    private static volatile double main_amp;
    private static double current_amp=0.0;
    private static int head_ptr=0,tail_ptr=0;
    static{
        Arrays.setAll(out_buffer,i->ByteBuffer.allocateDirect(buffer_size*4));
        for(ByteBuffer buffer:out_buffer) buffer.order(ByteOrder.LITTLE_ENDIAN);
        ALlib.init(new int[buffer_cnt],SoundEngine::mixTask);
        thread.setDaemon(true);
        thread.start();
    }
    public static void load(){
        is_run=false;
        ALlib.disable();
        ALlib.load();
        is_run=true;
        ALlib.enable();
        ALlib.clear();
    }
    private static void mixTask(){
        if(!is_run) return;
        try{
            ALlib.feed(out_buffer[tail_ptr]);
            tail_ptr++;
            if(tail_ptr==buffer_cnt) tail_ptr=0;
            synchronized(mix_lock){
                mix_lock.notify();
            }
        }
        catch(RuntimeException ignored){}
    }
    public static void setPause(boolean paused){
        is_paused=paused;
        if(is_paused) ALlib.pause();
        else ALlib.resume();
    }
    private static void mixLoop(){
        while(true){
            Arrays.fill(mix_buffer,0.0);
            out_buffer[head_ptr].clear();
            List<TrainData> train_datas=TrainStatus.getTrainData();
            TrainData.mixer.handle(mix_buffer,train_datas);
            double amp_step=(main_amp-current_amp)/buffer_size;
            for(int i=0;i<buffer_size;i++){
                current_amp+=amp_step;
                double clipped=Math.min(Math.max(mix_buffer[i],-1.0),1.0)*current_amp;
                short sample=(short)(clipped*Short.MAX_VALUE);
                out_buffer[head_ptr].putShort(sample);
                out_buffer[head_ptr].putShort(sample);
            }
            out_buffer[head_ptr].flip();
            head_ptr++;
            if(head_ptr==buffer_cnt) head_ptr=0;
            synchronized(mix_lock){
                try{
                    mix_lock.wait();
                }
                catch(InterruptedException ignored){}
            }
        }
    }
    @Override
    public void reload(){
        main_amp=Configs.main_amp.get();
    }
}