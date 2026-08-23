package createvvvfsim.network;
import createvvvfsim.types.FromType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
public record ModelEntry<T extends IModel>(FromType from,Type<T> type,
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec,IPayloadHandler<T> handler){
    public void register(PayloadRegistrar registrar){
        if(from()==FromType.client) registrar.playToServer(type,codec,handler);
        else if(from()==FromType.server) registrar.playToClient(type,codec,handler);
    }
}