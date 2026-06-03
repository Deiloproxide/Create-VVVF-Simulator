package createvvvfsim;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import vvvfsimulator.vvvf.modulation.CustomPwm;
@Mod(Configs.mod_id)
public class CreateVVVFSim{
    static{
        CustomPwm.CustomPwmPresets.preload();
    }
    public CreateVVVFSim(IEventBus modEventBus){
        modEventBus.addListener(CreateVVVFSim::register);
    }
    public static void register(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar=event.registrar(Configs.version);
        registrar.playToClient(TrainSyncModel.model_type,TrainSyncModel.stream_codec,TrainStatus::getServerSpeed);
    }
    public static int reloadCommand(CommandContext<CommandSourceStack> context){
        Component msg=Component.literal(Configs.command_return);
        TrainStatus.forceReload();
        context.getSource().sendSuccess(()->msg,false);
        return 1;
    }
}