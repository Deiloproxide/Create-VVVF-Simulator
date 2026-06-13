package vvvfsimulator.data.basefrequency;
public final class Manager{
    private static final Struct TEMPLATE=createTemplate();
    public static Struct loadData;
    public static Struct current=deepClone(TEMPLATE);
    public static String loadPath="";
    private static Struct createTemplate(){
        Struct template=new Struct();
        Struct.Point p1=new Struct.Point();
        p1.rate=5;
        p1.duration=20;
        p1.brake=false;
        p1.powerOn=true;
        p1.order=0;
        template.points.add(p1);
        Struct.Point p2=new Struct.Point();
        p2.rate=0;
        p2.duration=4;
        p2.brake=false;
        p2.powerOn=false;
        p2.order=1;
        template.points.add(p2);
        Struct.Point p3=new Struct.Point();
        p3.rate=5;
        p3.duration=20;
        p3.brake=true;
        p3.powerOn=true;
        p3.order=2;
        template.points.add(p3);
        return template;
    }
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