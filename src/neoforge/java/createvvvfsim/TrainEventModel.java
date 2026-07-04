package createvvvfsim;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;
public record TrainEventModel(UUID train_id,String name,String event,String dimension,
                              Vector3f pos) implements CustomPacketPayload{
    public static final Type<TrainEventModel> model_type=new Type<>(
            ResourceLocation.tryBuild(Configs.mod_id,Configs.event_name));
    public static final StreamCodec<RegistryFriendlyByteBuf,TrainEventModel> stream_codec=StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,TrainEventModel::train_id,
            ByteBufCodecs.STRING_UTF8,TrainEventModel::name,
            ByteBufCodecs.STRING_UTF8,TrainEventModel::event,
            ByteBufCodecs.STRING_UTF8,TrainEventModel::dimension,
            ByteBufCodecs.VECTOR3F,TrainEventModel::pos,
            TrainEventModel::new);
    static{
        CommonEvents.types.add(model_type);
    }
    @Override
    public Type<TrainEventModel> type(){
        return model_type;
    }
    @OnlyIn(Dist.CLIENT)
    public void handle(IPayloadContext ignored){
        ClientEvents.onGetTrainEvent(name(),event(),dimension(),pos());
        TrainStatus.getServerEvent(train_id());
    }
}