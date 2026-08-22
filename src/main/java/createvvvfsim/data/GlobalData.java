package createvvvfsim.data;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class GlobalData{
    public static final SlotData[] slots={new SlotData(),new SlotData()};
    public static final Set<TrainData> trains=ConcurrentHashMap.newKeySet();
    public static final Map<UUID,Double> cached_speeds=new ConcurrentHashMap<>();
    public static final Set<UUID> cached_events=ConcurrentHashMap.newKeySet();
}