package createvvvfsim;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
@Mod(Configs.mod_id)
public class CreateVVVFSim{
    public CreateVVVFSim(){
        FMLJavaModLoadingContext context=FMLJavaModLoadingContext.get();
        ModLoadingContext container=ModLoadingContext.get();
        container.registerConfig(ModConfig.Type.SERVER,Configs.server_config.get());
        container.registerConfig(ModConfig.Type.CLIENT,Configs.client_config.get());
        if(FMLEnvironment.dist.isClient()){
            ClientEvents.registerScreen(container);
            context.getModEventBus().addListener(ClientEvents::onInit);
        }
        CommonEvents.registerModel();
    }
}