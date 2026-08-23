package createvvvfsim.network;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public interface IModel extends CustomPacketPayload{
    Type<? extends CustomPacketPayload> type();
    default void handle(IPayloadContext ignored){}
}