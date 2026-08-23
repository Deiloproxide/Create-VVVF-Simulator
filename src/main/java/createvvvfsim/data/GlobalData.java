package createvvvfsim.data;
import createvvvfsim.reverber.PerfectedReverber;
import createvvvfsim.reverber.RemasteredReverber;
import createvvvfsim.reverber.Reverber;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class GlobalData{
    public static final SlotData[] slots={new SlotData(),new SlotData()};
    public static final Set<TrainData> trains=ConcurrentHashMap.newKeySet();
    public static final Map<UUID,Double> cached_speeds=new ConcurrentHashMap<>();
    public static final Set<UUID> cached_events=ConcurrentHashMap.newKeySet();
    public static final Reverber reverber;
    public static boolean server_available=false;
    static{
        if(RemasteredReverber.register()) reverber=new RemasteredReverber();
        else if(PerfectedReverber.register()) reverber=new PerfectedReverber();
        else reverber=new Reverber();
    }
}