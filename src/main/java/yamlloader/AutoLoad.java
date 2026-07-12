package yamlloader;
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
    static{
        try{
            hash=MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }
    public static Path getFilePath(Minecraft mc){
        IntegratedServer single_server=mc.getSingleplayerServer();
        if(single_server!=null) return single_server.getWorldPath(LevelResource.ROOT);
        ServerData data=mc.getCurrentServer();
        String hashed=hex.formatHex(hash.digest(data.ip.getBytes(StandardCharsets.UTF_8)));
        return mc.gameDirectory.toPath().resolve(Configs.autoload_dir).resolve(hashed);
    }
    public static void save(Minecraft mc,String yaml_name){
        if(yaml_name==null) return;
        Path path=getFilePath(mc).resolve(Configs.autoload_file),parent=path.getParent();
        Properties properties=new Properties();
        properties.setProperty(Configs.autoload_key,yaml_name);
        if(!Files.isDirectory(parent)){
            try{
                Files.createDirectories(parent);
            }
            catch(IOException ignored){}
        }
        try(Writer writer=Files.newBufferedWriter(path,StandardCharsets.UTF_8)){
            properties.store(writer,"");
        }
        catch(IOException ignored){}
    }
    public static String load(Minecraft mc){
        Path path=getFilePath(mc).resolve(Configs.autoload_file);
        if(!Files.isRegularFile(path)) return Configs.default_yaml;
        Properties properties=new Properties();
        String yaml_name=Configs.default_yaml;
        try(Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)){
            properties.load(reader);
            yaml_name=properties.getProperty(Configs.autoload_key,Configs.default_yaml);
        }
        catch(IOException ignored){}
        return yaml_name;
    }
}