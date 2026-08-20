package createvvvfsim.engine;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.LibFFI;
import org.lwjgl.system.MemoryUtil;
/**client class*/
public class ALCallback extends Callback implements CallbackI{
    private static final int buffer_complete=0x19A4;
    private static final FFICIF cif=APIUtil.apiCreateCIF(
            LibFFI.FFI_DEFAULT_ABI,LibFFI.ffi_type_void,LibFFI.ffi_type_sint32,LibFFI.ffi_type_uint32,
            LibFFI.ffi_type_uint32,LibFFI.ffi_type_sint32,LibFFI.ffi_type_pointer,LibFFI.ffi_type_pointer);
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
        int eventType=MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args));
        int object=MemoryUtil.memGetInt(MemoryUtil.memGetAddress(args+POINTER_SIZE));
        if(eventType==buffer_complete && object==ALlib.source_id) handler.run();
    }
}