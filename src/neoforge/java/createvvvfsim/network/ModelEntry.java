package createvvvfsim.network;
import createvvvfsim.types.PlayToType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
public record ModelEntry<T extends IModel>(PlayToType play_to,Type<T> type,
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec,IPayloadHandler<T> handler){
    public void register(PayloadRegistrar registrar){

    }
}