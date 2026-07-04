package createvvvfsim;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
public record TrainSyncModel(UUID train_id,double speed){
    public static void encode(TrainSyncModel model,FriendlyByteBuf buf){
        buf.writeUUID(model.train_id);
        buf.writeDouble(model.speed);
    }
    public static TrainSyncModel decode(FriendlyByteBuf buf){
        return new TrainSyncModel(buf.readUUID(),buf.readDouble());
    }
    @OnlyIn(Dist.CLIENT)
    public void handle(Supplier<NetworkEvent.Context> context){
        NetworkEvent.Context ctx=context.get();
        ctx.enqueueWork(()->TrainStatus.getServerSpeed(train_id(),speed()));
        ctx.setPacketHandled(true);
    }
}