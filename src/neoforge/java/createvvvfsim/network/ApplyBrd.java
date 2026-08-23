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
public record ApplyBrd(UUID train_id,int program,int motor,int base) implements IModel{
    public static final Type<ApplyBrd> model_type=new Type<>(
            ResourceLocation.tryBuild(ModConfig.mod_id,NetworkConfig.apply_brd));
    public static final StreamCodec<RegistryFriendlyByteBuf,ApplyBrd> codec=StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,ApplyBrd::train_id,
            ByteBufCodecs.INT,ApplyBrd::program,
            ByteBufCodecs.INT,ApplyBrd::motor,
            ByteBufCodecs.INT,ApplyBrd::base,
            ApplyBrd::new);
    @Override
    public Type<ApplyBrd> type(){
        return model_type;
    }
    @Override
    public void handle(IPayloadContext ignored){

    }
}