package soundphysics.remastered;
import java.lang.reflect.Method;
import java.util.Map;
public class RegisteredClass{
    public Class<?> clazz;
    Map<String,Method> methods;
    public RegisteredClass(Class<?> clazz,Map<String,Method> methods){
        this.clazz=clazz;
        this.methods=methods;
    }
}