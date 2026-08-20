package createvvvfsim.data;
import java.util.*;
public class GlobalData{
    public static final SlotData[] slots={new SlotData(),new SlotData()};
    public static final List<TrainData> all_trains=new ArrayList<>();
    public static final Map<UUID,Double> cached_speeds=new HashMap<>();
    public static final List<UUID> cached_events=new ArrayList<>();
    public static final Object train_lock=new Object();
    public static final Object speed_lock=new Object();
    public static final Object event_lock=new Object();
    public static List<TrainData> getAllTrains(){
        List<TrainData> train_datas;
        synchronized(train_lock){
            train_datas=new ArrayList<>(all_trains);
        }
        return train_datas;
    }
}