package createvvvfsim;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.Mod;
@Mod(Configs.mod_id)
public class CreateVVVFSim{
    private static final String mod_id=Configs.mod_id;
    public static final ResourceLocation sync_id=ResourceLocation.fromNamespaceAndPath(mod_id,Configs.sync_name);
    public static final ResourceLocation sound_id=ResourceLocation.fromNamespaceAndPath(mod_id,Configs.sound_name);
}