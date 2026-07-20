package createvvvfsim;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import org.joml.Vector3f;
public record TrainEventModel(UUID train_id,String name,String event,String dimension,Vector3f pos){
    public static void encode(TrainEventModel model,FriendlyByteBuf buf){
        buf.writeUUID(model.train_id);
        buf.writeUtf(model.name);
        buf.writeUtf(model.event);
        buf.writeUtf(model.dimension);
        buf.writeFloat(model.pos.x);
        buf.writeFloat(model.pos.y);
        buf.writeFloat(model.pos.z);
    }
    public static TrainEventModel decode(FriendlyByteBuf buf){
        return new TrainEventModel(buf.readUUID(),buf.readUtf(),buf.readUtf(),buf.readUtf(),
                new Vector3f(buf.readFloat(),buf.readFloat(),buf.readFloat()));
    }
    public void handle(Supplier<Context> context){
        Context ctx=context.get();
        ctx.enqueueWork(()->{
            ClientEvents.onGetTrainEvent(name(),event(),dimension(),pos());
            TrainStatus.getServerEvent(train_id());
        });
        ctx.setPacketHandled(true);
    }
}