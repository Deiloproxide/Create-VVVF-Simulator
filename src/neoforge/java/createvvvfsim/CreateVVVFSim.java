package createvvvfsim;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
@Mod(Configs.mod_id)
public class CreateVVVFSim{
    public CreateVVVFSim(ModContainer container){
        container.registerConfig(ModConfig.Type.SERVER,Configs.server_config.get());
        container.registerConfig(ModConfig.Type.CLIENT,Configs.client_config.get());
        if(FMLEnvironment.dist.isClient()) ClientEvents.registerScreen(container);
    }
}