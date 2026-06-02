package createvvvfsim;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import soundphysics.remastered.SoundPhysicsBridgeManager;
import vvvfsimulator.vvvf.modulation.CustomPwm;
@Mod(CreateVVVFSim.mod_id)
public class CreateVVVFSim{
    public static final String mod_id="create_vvvf_simulator";
    static{
        SoundPhysicsBridgeManager.init();
        CustomPwm.CustomPwmPresets.preload();
    }
    public CreateVVVFSim(IEventBus modEventBus){
        modEventBus.addListener(CreateVVVFSim::register);
    }
    public static void register(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar=event.registrar("1.0.0");
        registrar.playToClient(TrainSyncModel.model_type,TrainSyncModel.stream_codec,TrainStatus::getServerSpeed);
    }
}