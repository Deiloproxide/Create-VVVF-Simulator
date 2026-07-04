package createvvvfsim;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
@Mod(Configs.mod_id)
public class CreateVVVFSim{
    public CreateVVVFSim(FMLJavaModLoadingContext context){
        ModContainer container=context.getContainer();
        container.addConfig(new ModConfig(ModConfig.Type.SERVER,Configs.server_config.get(),container));
        container.addConfig(new ModConfig(ModConfig.Type.CLIENT,Configs.client_config.get(),container));
        if(FMLEnvironment.dist.isClient()){
            ClientEvents.registerScreen(container);
            context.getModEventBus().addListener(ClientEvents::onInit);
        }
        CommonEvents.registerModel();
    }
}