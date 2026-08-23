package createvvvfsim.config;
import createvvvfsim.types.FromType;
import createvvvfsim.types.SlotType;
import java.util.ArrayList;
import java.util.List;
public class CommandConfig{
    public static final String vvvf="vvvf";
    public static final String op_list="list";
    public static final String op_upload="upload";
    public static final String slot_num="slot_num";
    public static final String file_name="file_name";
    public static final List<String> list_from;
    public static final List<String> upload_from;
    public static final List<String> list_slot;
    public static final List<String> upload_slot;
    static{
        upload_from=new ArrayList<>();
        for(FromType type:FromType.values()) upload_from.add(type.name());
        list_from=new ArrayList<>(upload_from);
        list_from.add("all");
        upload_slot=new ArrayList<>();
        for(SlotType type:SlotType.values()) upload_slot.add(type.name());
        list_slot=new ArrayList<>(upload_slot);
        list_slot.add("all");
    }
}