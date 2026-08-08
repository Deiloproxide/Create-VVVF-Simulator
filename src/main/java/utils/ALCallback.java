package utils;
import genengine.SoundEngine;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.libffi.FFICIF;
import static org.lwjgl.system.APIUtil.apiCreateCIF;
import static org.lwjgl.system.MemoryUtil.memGetAddress;
import static org.lwjgl.system.MemoryUtil.memGetInt;
import static org.lwjgl.system.libffi.LibFFI.*;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_pointer;
/**client class*/
public class ALCallback extends Callback implements CallbackI{
    private static final int buffer_complete=0x19A4;
    private static final FFICIF cif=apiCreateCIF(FFI_DEFAULT_ABI,
            ffi_type_void,ffi_type_sint32,ffi_type_uint32,ffi_type_uint32,
            ffi_type_sint32,ffi_type_pointer,ffi_type_pointer);
    private final Runnable handler=SoundEngine::mixTask;
    public ALCallback(){
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
        if(eventType==buffer_complete && object==ALlib.source_id) handler.run();
    }
}