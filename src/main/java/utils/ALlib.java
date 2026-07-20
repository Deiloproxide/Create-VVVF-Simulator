package utils;
import createvvvfsim.Configs;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.openal.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libffi.FFICIF;
import static org.lwjgl.system.APIUtil.apiCreateCIF;
import static org.lwjgl.system.JNI.invokePP;
import static org.lwjgl.system.JNI.invokePPV;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memGetAddress;
import static org.lwjgl.system.MemoryUtil.memGetInt;
import static org.lwjgl.system.libffi.LibFFI.FFI_DEFAULT_ABI;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_pointer;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_sint32;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_uint32;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_void;
public final class ALlib{
    private static final int sample_rate=Configs.sample_rate.get();
    private static final int buffer_size=Configs.buffer_size.get();
    private static final int buffer_complete=0x19A4;
    private static final ByteBuffer empty=ByteBuffer.allocateDirect(buffer_size*4);
    private static final EventCallback callback=new EventCallback();
    private static long control_addr=NULL,callback_addr=NULL;
    private static volatile int source_id=0;
    private static int[] al_buffers;
    private static Runnable handler;
    public static void init(int[] buffers,Runnable task){
        al_buffers=buffers;
        handler=task;
    }
    public static void load(){
        AL10.alDeleteSources(source_id);
        AL10.alDeleteBuffers(al_buffers);
        AL.getCapabilities();
        source_id=AL10.alGenSources();
        AL10.alGenBuffers(al_buffers);
        AL10.alSourcei(source_id,AL10.AL_SOURCE_RELATIVE,AL10.AL_TRUE);
        AL10.alSourcef(source_id,AL10.AL_GAIN,1f);
        AL10.alSourcef(source_id,AL10.AL_ROLLOFF_FACTOR,0f);
        AL10.alSource3f(source_id,AL10.AL_POSITION,0f,0f,0f);
        AL10.alSourcei(source_id,SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT,
                SOFTDirectChannelsRemix.AL_REMIX_UNMATCHED_SOFT);
        for(int al_buffer:al_buffers)
            AL10.alBufferData(al_buffer,AL10.AL_FORMAT_STEREO16,empty,sample_rate);
        AL10.alSourceQueueBuffers(source_id,al_buffers);
        resume();
    }
    public static void feed(ByteBuffer buffer){
        int al_buffer=AL10.alSourceUnqueueBuffers(source_id);
        AL10.alBufferData(al_buffer,AL10.AL_FORMAT_STEREO16,buffer,sample_rate);
        AL10.alSourceQueueBuffers(source_id,al_buffer);
        if(AL10.alGetSourcei(source_id,AL10.AL_SOURCE_STATE)!=AL10.AL_PLAYING) resume();
    }
    public static void pause(){
        AL10.alSourcePause(source_id);
    }
    public static void resume(){
        AL10.alSourcePlay(source_id);
    }
    public static void enable(){
        try{
            boolean present=AL10.alIsExtensionPresent("AL_SOFT_events");
            if(!present) return;
            control_addr=getAddr("alEventControlSOFT");
            callback_addr=getAddr("alEventCallbackSOFT");
            if(control_addr==NULL || callback_addr==NULL) return;
            invokePPV(callback.address(),NULL,callback_addr);
            control(true);
        }
        catch(RuntimeException ignored){}
    }
    public static void disable(){
        try{
            if(control_addr!=NULL) control(false);
            if(callback_addr!=NULL) invokePPV(NULL,NULL,callback_addr);
        }
        catch(RuntimeException ignored){}
    }
    public static void clear(){
        AL10.alGetError();
    }
    private static long getAddr(String name){
        long alGetProcAddress=ALC.getFunctionProvider()
                .getFunctionAddress(NULL,"alGetProcAddress");
        if(alGetProcAddress==NULL) return NULL;
        try(MemoryStack stack=MemoryStack.stackPush()){
            ByteBuffer functionName=stack.ASCII(name,true);
            return invokePP(memAddress(functionName),alGetProcAddress);
        }
    }
    private static void control(boolean enable){
        try(MemoryStack stack=MemoryStack.stackPush()){
            IntBuffer types=stack.mallocInt(1);
            types.put(0,buffer_complete);
            invokePPV(1L,memAddress(types),enable,control_addr);
        }
    }
    private static final class EventCallback extends Callback implements CallbackI{
        private static final FFICIF cif=apiCreateCIF(FFI_DEFAULT_ABI,
                ffi_type_void,ffi_type_sint32,ffi_type_uint32,ffi_type_uint32,
                ffi_type_sint32,ffi_type_pointer,ffi_type_pointer);
        public EventCallback(){
            super(cif);
        }
        @Override
        public FFICIF getCallInterface(){
            return cif;
        }
        @Override
        public void callback(long ret,long args){
            int eventType=memGetInt(memGetAddress(args));
            int object=memGetInt(memGetAddress(args+POINTER_SIZE));
            if(eventType==buffer_complete && object==source_id) handler.run();
        }
    }
}