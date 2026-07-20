package vvvfsimulator.data.trainaudio;
public class Manager{
    private static final Struct TEMPLATE=new Struct();
    public static Struct loadData;
    public static Struct current=deepClone(TEMPLATE);
    public static String loadPath="";
    public static Struct deepClone(Struct source){
        return source.copy();
    }
    public static Struct getTemplate(){
        return deepClone(TEMPLATE);
    }
    public static void resetCurrent(){
        current=deepClone(TEMPLATE);
        loadPath="";
        loadData=null;
    }
}