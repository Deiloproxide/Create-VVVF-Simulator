package createvvvfsim.network;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.NetworkConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
/**server to client class*/
public record FileLoad(int slot_num,String file_type,String file_name,byte[] content) implements IModel{
    public static final Type<FileLoad> model_type=new Type<>(
            ResourceLocation.tryBuild(ModConfig.mod_id,NetworkConfig.file_brd));
    public static final StreamCodec<RegistryFriendlyByteBuf,FileLoad> codec=StreamCodec.composite(
            ByteBufCodecs.INT,FileLoad::slot_num,
            ByteBufCodecs.STRING_UTF8,FileLoad::file_type,
            ByteBufCodecs.STRING_UTF8,FileLoad::file_name,
            ByteBufCodecs.BYTE_ARRAY,FileLoad::content,
            FileLoad::new);
    @Override
    public Type<FileLoad> type(){
        return model_type;
    }
    @Override
    public void handle(IPayloadContext ignored){

    }
}