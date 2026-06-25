package createvvvfsim;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
public record TrainSyncModel(UUID train_id,double speed){
    public static final SimpleChannel channel=NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild(Configs.mod_id,Configs.sync_name),
            ()->Configs.version,Configs.version::equals,Configs.version::equals);
    private static int id=0;
    public static void register(){
        channel.messageBuilder(TrainSyncModel.class,id++,NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TrainSyncModel::encode).decoder(TrainSyncModel::decode)
                .consumerMainThread(TrainSyncModel::onGetSpeed).add();
    }
    private static void encode(TrainSyncModel model,FriendlyByteBuf buf){
        buf.writeUUID(model.train_id);
        buf.writeDouble(model.speed);
    }
    private static TrainSyncModel decode(FriendlyByteBuf buf){
        return new TrainSyncModel(buf.readUUID(),buf.readDouble());
    }
    public static void onGetSpeed(TrainSyncModel model,Supplier<NetworkEvent.Context> context){
        NetworkEvent.Context ctx=context.get();
        ctx.enqueueWork(()->TrainStatus.getServerSpeed(model.train_id(),model.speed()));
        ctx.setPacketHandled(true);
    }
}