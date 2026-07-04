package createvvvfsim;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
@EventBusSubscriber(modid=Configs.mod_id)
public class CommonEvents{
    @SubscribeEvent
    public static void registerModel(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar=event.registrar(Configs.version).optional();
        registrar.playToClient(TrainSyncModel.model_type,
                TrainSyncModel.stream_codec,TrainSyncModel::handle);
        registrar.playToClient(TrainEventModel.model_type,
                TrainEventModel.stream_codec,TrainEventModel::handle);
    }
}