package createvvvfsim.config;
import createvvvfsim.util.*;
import java.util.List;
public class SpecConfig{
    public static final ConfigSpec server_config;
    public static final ConfigSpec client_config;
    public static final IntValue upload_permission;
    public static final IntValue sync_period;
    public static final IntValue eval_period;
    public static final IntValue sample_rate;
    public static final IntValue buffer_cnt;
    public static final IntValue buffer_size;
    public static final IntValue conv_size;
    public static final IntValue tail_size;
    public static final IntValue speeds_length;
    public static final DoubleValue max_acc_ratio;
    public static final DoubleValue near_distance;
    public static final DoubleValue far_distance;
    public static final BooleanValue mute_event;
    static{
        Builder server_builder=new Builder();
        upload_permission=server_builder.defineInRange("upload_permission",4,0,4);
        sync_period=server_builder.defineInRange("sync_period",3,1,20);
        server_config=server_builder.build();
        Builder client_builder=new Builder();
        client_builder.push("environment");
        eval_period=client_builder.defineInRange("eval_period",3,1,20);
        client_builder.pop();
        client_builder.push("audio");
        sample_rate=client_builder.defineInRange("sample_rate",44100,8000,192000);
        buffer_cnt=client_builder.defineInRange("buffer_cnt",4,2,8);
        buffer_size=client_builder.defineInRange("buffer_size",1<<12,1<<10,1<<16);
        conv_size=client_builder.defineInRange("conv_size",1<<10,1<<8,1<<12);
        tail_size=client_builder.defineInRange("tail_size",1<<18,1<<16,1<<20);
        client_builder.pop();
        client_builder.push("speed_smoother");
        speeds_length=client_builder.defineInRange("speeds_length",5,1,20);
        max_acc_ratio=client_builder.defineInRange("max_acc_ratio",1.05,1.0,1.2);
        client_builder.pop();
        client_builder.push("spread_distance");
        near_distance=client_builder.defineInRange("near_distance",32.0,0.0,256.0);
        far_distance=client_builder.defineInRange("far_distance",96.0,0.0,256.0);
        client_builder.pop();
        mute_event=client_builder.define("mute_event",false);
        client_config=client_builder.build();
    }
}