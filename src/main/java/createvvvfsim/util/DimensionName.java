package createvvvfsim.util;
import createvvvfsim.config.PathConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
/**client class*/
public class DimensionName{
    public static String get(String dim_mod,String dim_name){
        if("minecraft".equals(dim_mod))
            return I18n.get(PathConfig.dimension_path+dim_name);
        String key="dimension."+dim_mod+"."+dim_name;
        String fall_back=StringUtils.capitalize(dim_name.replace('_',' '));
        return Component.translatableWithFallback(key,fall_back).getString();
    }
}