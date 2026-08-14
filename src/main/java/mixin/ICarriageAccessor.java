package mixin;
import com.simibubi.create.content.trains.entity.Carriage;
import createvvvfsim.Configs;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
/**server class*/
@Mixin(value=Carriage.class,remap=false,priority=Configs.mixin_priority)
public interface ICarriageAccessor{
    @Accessor("entities")
    Map<ResourceKey<Level>,Carriage.DimensionalCarriageEntity> entities();
}