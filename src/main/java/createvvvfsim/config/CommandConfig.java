package createvvvfsim.config;
import createvvvfsim.types.FromType;
import createvvvfsim.types.SlotType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class CommandConfig{
    public static final String vvvf="vvvf";
    public static final String op_list="list";
    public static final String op_upload="upload";
    public static final String slot_num="slot_num";
    public static final String file_name="file_name";
    public static final Set<String> list_from;
    public static final Set<String> upload_from;
    public static final Set<String> list_slot;
    public static final Set<String> upload_slot;
    static{
        upload_from=new HashSet<>();
        for(FromType type:FromType.values()) upload_from.add(type.name());
        list_from=new HashSet<>(upload_from);
        list_from.add("all");
        upload_slot=new HashSet<>();
        for(SlotType type:SlotType.values()) upload_slot.add(type.name());
        list_slot=new HashSet<>(upload_slot);
        list_slot.add("all");
    }
}