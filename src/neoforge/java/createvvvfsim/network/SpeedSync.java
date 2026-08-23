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
/**server to client class*/
public record SpeedSync(UUID train_id,double speed) implements IModel{
    public static final Type<SpeedSync> model_type=new Type<>(
            ResourceLocation.tryBuild(ModConfig.mod_id,NetworkConfig.speed_sync));
    public static final StreamCodec<RegistryFriendlyByteBuf,SpeedSync> codec=StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,SpeedSync::train_id,
            ByteBufCodecs.DOUBLE,SpeedSync::speed,
            SpeedSync::new);
    @Override
    public Type<SpeedSync> type(){
        return model_type;
    }
    @Override
    public void handle(IPayloadContext ignored){

    }
}