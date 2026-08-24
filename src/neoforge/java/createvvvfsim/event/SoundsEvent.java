package createvvvfsim.event;
import createvvvfsim.config.ModConfig;
import createvvvfsim.config.PathConfig;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class SoundsEvent{
    private static final String mod_id=ModConfig.mod_id;
    public static final DeferredRegister<SoundEvent> sound_event=
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT,mod_id);
    public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> sounds=new HashMap<>();
    public static void register(IEventBus bus){
        for(String name: PathConfig.gen_path){
            ResourceLocation location=ResourceLocation.tryBuild(mod_id,PathConfig.sound_path+name);
            DeferredHolder<SoundEvent,SoundEvent> holder=sound_event.register(
                    name,()->SoundEvent.createVariableRangeEvent(location));
            sounds.put(name,holder);
        }
        sound_event.register(bus);
    }
}