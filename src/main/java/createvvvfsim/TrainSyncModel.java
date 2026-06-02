package createvvvfsim;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record TrainSyncModel(UUID train_id,double speed) implements CustomPacketPayload{
    public static final Type<TrainSyncModel> model_type=new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateVVVFSim.mod_id,"train_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf,TrainSyncModel> stream_codec=StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,TrainSyncModel::train_id,
            ByteBufCodecs.DOUBLE,TrainSyncModel::speed,
            TrainSyncModel::new);
    @Override
    public Type<TrainSyncModel> type(){
        return model_type;
    }
}