package createvvvfsim.mixin;
import com.simibubi.create.content.trains.entity.Carriage;
import createvvvfsim.config.ModConfig;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=Carriage.class,remap=false,priority=ModConfig.mixin_priority)
public interface ICarriageAccessor{
    @Accessor("entities")
    Map<ResourceKey<Level>,Carriage.DimensionalCarriageEntity> entities();
}