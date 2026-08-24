package createvvvfsim.event;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.NetworkConfig;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
@EventBusSubscriber(modid=ModConfig.mod_id)
public class CommonEvent{
    public static final Set<Type<?>> types=new HashSet<>();
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar=event.registrar(NetworkConfig.version).optional();

    }
}