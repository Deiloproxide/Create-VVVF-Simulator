package createvvvfsim.data;
import java.util.Arrays;

import createvvvfsim.config.ModConfig;
import createvvvfsim.types.SlotType;
import vvvfsimulator.data.vvvf.Struct;
public class SlotData{
    public static final int slot_num=ModConfig.slot_num;
    public final String[][] names=new String[SlotType.values().length][slot_num];
    public final Struct[] programs=new Struct[slot_num];
    public final vvvfsimulator.data.trainaudio.Struct[] motors=new vvvfsimulator.data.trainaudio.Struct[slot_num];
    public final BaseData[] bases=new BaseData[slot_num];
    public SlotData(){
        for(String[] name:names) Arrays.fill(name,"-");
        Arrays.fill(programs,new Struct());
        Arrays.fill(motors,new vvvfsimulator.data.trainaudio.Struct());
        Arrays.fill(bases,new BaseData());
    }
}