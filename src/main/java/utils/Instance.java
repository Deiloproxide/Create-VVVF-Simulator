package utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
public class Instance{
    public final Class<?> clazz;
    public final Object object;
    public Instance(String class_name) throws ReflectiveOperationException{
        this.clazz=Class.forName(class_name);
        this.object=null;
    }
    public Instance(Constructor<?> constructor,Object... args) throws ReflectiveOperationException{
        this.clazz=constructor.getDeclaringClass();
        this.object=constructor.newInstance(args);
    }
    public Instance(Object object){
        this.clazz=object==null?null:object.getClass();
        this.object=object;
    }
    public Constructor<?> getConstructor(Class<?>... args) throws ReflectiveOperationException{
        return clazz.getConstructor(args);
    }
    public Method getMethod(String method_name,Class<?>... args) throws ReflectiveOperationException{
        return clazz.getMethod(method_name,args);
    }
    public <T> T invoke(Class<T> type,Method method,Object... args) throws ReflectiveOperationException{
        return type.cast(method.invoke(object,args));
    }
    public Instance invoke(Method method,Object... args) throws ReflectiveOperationException{
        return new Instance(method.invoke(object,args));
    }
    public static <T> T invokeStatic(Class<T> type,Method method,Object... args) throws ReflectiveOperationException{
        return type.cast(method.invoke(null,args));
    }
    public static Instance invokeStatic(Method method,Object... args) throws ReflectiveOperationException{
        return new Instance(method.invoke(null,args));
    }
    public <T> T get(Class<T> type,String field_name) throws ReflectiveOperationException{
        return type.cast(clazz.getField(field_name).get(object));
    }
    public Instance get(String field_name) throws ReflectiveOperationException{
        return new Instance(clazz.getField(field_name).get(object));
    }
}