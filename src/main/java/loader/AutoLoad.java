package loader;
import createvvvfsim.Configs;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
public class AutoLoad{
    private static final HexFormat hex=HexFormat.of();
    private static final MessageDigest hash;
    public static final Properties properties=new Properties();
    static{
        properties.setProperty(Configs.loadyaml_key,Configs.default_yaml);
        properties.setProperty(Configs.loadir_key,Configs.default_ir);
        try{
            hash=MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
    private static Path getFilePath(Minecraft mc){
        IntegratedServer single_server=mc.getSingleplayerServer();
        if(single_server!=null) return single_server.getWorldPath(LevelResource.ROOT);
        ServerData data=mc.getCurrentServer();
        String hashed=hex.formatHex(hash.digest(data.ip.getBytes(StandardCharsets.UTF_8)));
        return mc.gameDirectory.toPath().resolve(Configs.autoload_dir).resolve(hashed);
    }
    public static void save(Minecraft mc){
        Path path=getFilePath(mc).resolve(Configs.autoload_file),parent=path.getParent();
        if(!Files.isDirectory(parent)){
            try{
                Files.createDirectories(parent);
            }
            catch(IOException ignored){}
        }
        if(YamlLoader.success_name!=null)
            properties.setProperty(Configs.loadyaml_key,YamlLoader.success_name);
        if(IRLoader.success_name!=null)
            properties.setProperty(Configs.loadir_key,IRLoader.success_name);
        try(Writer writer=Files.newBufferedWriter(path,StandardCharsets.UTF_8)){
            properties.store(writer,"");
        }
        catch(IOException ignored){}
    }
    public static String load(Minecraft mc){
        Path path=getFilePath(mc).resolve(Configs.autoload_file);
        try(Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){
            properties.load(reader);
        }
        catch(IOException ignored){}
        String yaml_msg=YamlLoader.loadYaml(properties.getProperty(Configs.loadyaml_key));
        String ir_msg=IRLoader.loadIR(properties.getProperty(Configs.loadir_key));
        return String.join("\n",yaml_msg,ir_msg);
    }
}