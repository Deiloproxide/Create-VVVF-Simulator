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
import org.joml.Vector3f;
/**server to client class*/
public record EventBrd(UUID train_id,String name,String event,String dim_mod,
                       String dim_name,Vector3f pos) implements IModel{
    public static final Type<EventBrd> model_type=new Type<>(
            ResourceLocation.tryBuild(ModConfig.mod_id,NetworkConfig.event_brd));
    public static final StreamCodec<RegistryFriendlyByteBuf,EventBrd> codec=StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,EventBrd::train_id,
            ByteBufCodecs.STRING_UTF8,EventBrd::name,
            ByteBufCodecs.STRING_UTF8,EventBrd::event,
            ByteBufCodecs.STRING_UTF8,EventBrd::dim_mod,
            ByteBufCodecs.STRING_UTF8,EventBrd::dim_name,
            ByteBufCodecs.VECTOR3F,EventBrd::pos,
            EventBrd::new);
    @Override
    public Type<EventBrd> type(){
        return model_type;
    }
    @Override
    public void handle(IPayloadContext ignored){

    }
}