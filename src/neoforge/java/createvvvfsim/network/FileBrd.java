package createvvvfsim.network;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.NetworkConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
/**server to client class*/
public record FileBrd(int slot_num,String file_type,String file_name,byte[] content) implements CustomPacketPayload{
    public static final Type<FileBrd> model_type=new Type<>(
            ResourceLocation.tryBuild(ModConfig.mod_id,NetworkConfig.file_brd));
    public static final StreamCodec<RegistryFriendlyByteBuf,FileBrd> stream_codec=StreamCodec.composite(
            ByteBufCodecs.INT,FileBrd::slot_num,
            ByteBufCodecs.STRING_UTF8,FileBrd::file_type,
            ByteBufCodecs.STRING_UTF8,FileBrd::file_name,
            ByteBufCodecs.BYTE_ARRAY,FileBrd::content,
            FileBrd::new);
    static{

    }
    @Override
    public Type<FileBrd> type(){
        return model_type;
    }
    public void handle(IPayloadContext ignored){

    }
}