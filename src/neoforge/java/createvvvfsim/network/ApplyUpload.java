package createvvvfsim.network;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.NetworkConfig;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
public record ApplyUpload(UUID train_id,int program,int motor,int base) implements IModel{
    public static final Type<ApplyUpload> model_type=new Type<>(
            ResourceLocation.tryBuild(ModConfig.mod_id,NetworkConfig.apply_upload));
    public static final StreamCodec<RegistryFriendlyByteBuf,ApplyUpload> codec=StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,ApplyUpload::train_id,
            ByteBufCodecs.INT,ApplyUpload::program,
            ByteBufCodecs.INT,ApplyUpload::motor,
            ByteBufCodecs.INT,ApplyUpload::base,
            ApplyUpload::new);
    @Override
    public Type<ApplyUpload> type(){
        return model_type;
    }
    @Override
    public void handle(IPayloadContext ignored){

    }
}