package createvvvfsim;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
public class CommonEvents{
    public static final SimpleChannel channel=NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild(Configs.mod_id,Configs.group_id),
            ()->Configs.version,Configs.version::equals,Configs.version::equals);
    public static int id=0;
    public static void registerModel(){
        channel.messageBuilder(TrainSyncModel.class,id++,NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TrainSyncModel::encode).decoder(TrainSyncModel::decode)
                .consumerMainThread(TrainSyncModel::handle).add();
        channel.messageBuilder(TrainEventModel.class,id++,NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TrainEventModel::encode).decoder(TrainEventModel::decode)
                .consumerMainThread(TrainEventModel::handle).add();
    }
}