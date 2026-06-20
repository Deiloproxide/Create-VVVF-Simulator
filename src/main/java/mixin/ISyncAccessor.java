package mixin;
import com.simibubi.create.content.trains.entity.CarriageSyncData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=CarriageSyncData.class,remap=false)
public interface ISyncAccessor{
    @Accessor("pointDistanceSnapshot")
    float[] pointDistanceSnapshot();
    @Accessor("ticksSince")
    int ticksSince();
}