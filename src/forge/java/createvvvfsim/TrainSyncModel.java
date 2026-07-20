package createvvvfsim;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
public record TrainSyncModel(UUID train_id,double speed){
    public static void encode(TrainSyncModel model,FriendlyByteBuf buf){
        buf.writeUUID(model.train_id);
        buf.writeDouble(model.speed);
    }
    public static TrainSyncModel decode(FriendlyByteBuf buf){
        return new TrainSyncModel(buf.readUUID(),buf.readDouble());
    }
    public void handle(Supplier<Context> context){
        Context ctx=context.get();
        ctx.enqueueWork(()->TrainStatus.getServerSpeed(train_id(),speed()));
        ctx.setPacketHandled(true);
    }
}