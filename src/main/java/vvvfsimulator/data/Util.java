package vvvfsimulator.data;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
public class Util{
    public static String getPropertyValues(Object object){
        if(object==null) return "null";
        StringBuilder out=new StringBuilder();
        Field[] fields=object.getClass().getFields();
        boolean first=true;
        for(Field field:fields){
            if(Modifier.isStatic(field.getModifiers())) continue;
            if(!first) out.append(", ");
            first=false;
            out.append(field.getName()).append(" : ");
            try{
                out.append(getPropertyValue(field.get(object)));
            }
            catch(IllegalAccessException e){
                out.append("<inaccessible>");
            }
        }
        return out.toString();
    }
    private static String getPropertyValue(Object value){
        if(value==null) return "null";
        if(value.getClass().isArray()){
            StringBuilder out=new StringBuilder("[");
            int len=Array.getLength(value);
            for(int i=0;i<len;i++){
                if(i>0) out.append(", ");
                out.append(getPropertyValue(Array.get(value,i)));
            }
            out.append(']');
            return out.toString();
        }
        if(value instanceof Collection<?> collection){
            StringBuilder out=new StringBuilder("[");
            boolean first=true;
            for(Object item:collection){
                if(!first) out.append(", ");
                first=false;
                out.append(getPropertyValue(item));
            }
            out.append(']');
            return out.toString();
        }
        return String.valueOf(value);
    }
}