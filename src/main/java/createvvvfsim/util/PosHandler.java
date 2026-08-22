package createvvvfsim.util;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import createvvvfsim.mixin.ICarriageAccessor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
/**common class*/
public class PosHandler{
    private static boolean has_sable=true;
    private static Instance sable_helper;
    private static Method out_project;
    static{
        try{
            Instance sable=new Instance("dev.ryanhcode.sable.Sable");
            sable_helper=sable.get("HELPER");
            out_project=sable_helper.getMethod("projectOutOfSubLevel",Level.class,Vec3.class);
        }
        catch(Throwable ignored){
            has_sable=false;
        }
    }
    public static Tuple<Vec3,CarriageContraptionEntity> getCarriageInf(Carriage carriage,Level level){
        DimensionalCarriageEntity dce=carriage.getDimensionalIfPresent(level.dimension());
        if(dce==null) return null;
        CarriageContraptionEntity entity=dce.entity.get();
        if(entity==null) return null;
        if(entity.isRemoved()) return null;
        return new Tuple<>(convert(entity.position(),level),entity);
    }
    public static Map<ResourceKey<Level>,Vec3> getTrainPos(Train train){
        Map<ResourceKey<Level>,Vec3> positions=new HashMap<>();
        for(Carriage carriage:train.carriages){
            Map<ResourceKey<Level>,DimensionalCarriageEntity> entities=((ICarriageAccessor)carriage).entities();
            for(Entry<ResourceKey<Level>,DimensionalCarriageEntity> entry:entities.entrySet()){
                ResourceKey<Level> dimension=entry.getKey();
                Vec3 position=Vec3.atLowerCornerOf(BlockPos.containing(entry.getValue().positionAnchor));
                positions.putIfAbsent(dimension,position);
            }
        }
        return positions;
    }
    public static Vec3 convert(Vec3 train_pos,Level level){
        if(has_sable){
            try{
                return sable_helper.invoke(Vec3.class,out_project,level,train_pos);
            }
            catch(ReflectiveOperationException ignored){}
        }
        return train_pos;
    }
}