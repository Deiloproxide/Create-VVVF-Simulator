package createvvvfsim.engine;
import createvvvfsim.config.SpecConfig;
import createvvvfsim.data.GlobalData;
import createvvvfsim.reverber.PerfectedReverber;
import createvvvfsim.reverber.RemasteredReverber;
import createvvvfsim.reverber.Reverber;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
/**client class*/
public class SoundEngine{
    private static final int buffer_size=SpecConfig.buffer_size.get();
    private static final int buffer_cnt=SpecConfig.buffer_cnt.get();
    private static final double[] mix_buffer=new double[buffer_size];
    private static final ByteBuffer[] out_buffer=new ByteBuffer[buffer_cnt];
    private static final Thread thread=new Thread(SoundEngine::mixLoop);
    private static final Object buffer_lock=new Object();
    private static final Reverber reverber=GlobalData.reverber;
    private static volatile boolean is_run=false,is_paused=false;
    private static volatile int buffer_remain=buffer_cnt;
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
            reverber.handle(mix_buffer);
            for(int i=0;i<buffer_size;i++){
                double clipped=Math.min(Math.max(mix_buffer[i],-1.0),1.0);
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
}