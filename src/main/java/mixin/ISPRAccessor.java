package mixin;
import createvvvfsim.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;
/**client class*/
@Pseudo
@Mixin(targets="com.sonicether.soundphysics.SoundPhysics",remap=false,priority=Configs.mixin_priority)
public interface ISPRAccessor{
    @Invoker("calculateOcclusion")
    static double calculateOcclusion(Vec3 soundPos,Vec3 playerPos,SoundSource category,ResourceLocation sound){
        throw new AssertionError();
    }
    @Invoker("getBlockReflectivity")
    static float getBlockReflectivity(BlockPos blockPos){
        throw new AssertionError();
    }
    @Invoker("reflect")
    static Vec3 reflect(Vec3 dir,Vec3 normal){
        throw new AssertionError();
    }
    @Invoker("getSharedAirspace")
    static Vec3 getSharedAirspace(BlockHitResult hit,Vec3 listenerPosition){
        throw new AssertionError();
    }
    @Invoker("getSharedAirspace")
    static Vec3 getSharedAirspace(Vec3 soundPosition,Vec3 listenerPosition){
        throw new AssertionError();
    }
}