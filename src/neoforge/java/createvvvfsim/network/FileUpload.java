package createvvvfsim.network;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.NetworkConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
/**client to server class*/
public record FileUpload(int slot_num,String file_type,String file_name,byte[] content) implements CustomPacketPayload{
    public static final Type<FileUpload> model_type=new Type<>(
            ResourceLocation.tryBuild(ModConfig.mod_id,NetworkConfig.file_upload));
    public static final StreamCodec<RegistryFriendlyByteBuf,FileUpload> stream_codec=StreamCodec.composite(
            ByteBufCodecs.INT,FileUpload::slot_num,
            ByteBufCodecs.STRING_UTF8,FileUpload::file_type,
            ByteBufCodecs.STRING_UTF8,FileUpload::file_name,
            ByteBufCodecs.BYTE_ARRAY,FileUpload::content,
            FileUpload::new);
    static{

    }
    @Override
    public Type<FileUpload> type(){
        return model_type;
    }
    public void handle(IPayloadContext ignored){

    }
}