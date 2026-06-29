package soundphysics;
import createvvvfsim.Configs;
import createvvvfsim.EnvData;
import createvvvfsim.TrainData;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import mixin.ISPRAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import utils.Instance;
import utils.Lowpass;
public class RemasteredHandler extends Handler{
    private static final int buffer_size=Configs.buffer_size.get();
    private static final int tail_size=Configs.tail_size.get();
    private static final float angle=(float)(Math.PI*(Math.sqrt(5f)+1f));
    private static final double filters_alpha=0.82,filter_alpha=0.92;
    private static volatile double far_distance;
    private static final double[] train_buffer=new double[buffer_size];
    private static final double[][] tail_buffers=new double[4][tail_size];
    private static final Lowpass[] filters=new Lowpass[5];
    private static ResourceLocation sound_id;
    private static Constructor<?> constructor;
    private static Method add_direct,add_shared,get_shared,ray_cast;
    private static int head_ptr=0;
    static{
        Arrays.setAll(filters,i->new Lowpass());
    }
    public static boolean register(){
        try{
            sound_id=ResourceLocation.tryBuild(Configs.mod_id,Configs.sound_name);
            Instance reflected_audio=new Instance("com.sonicether.soundphysics.ReflectedAudio");
            constructor=reflected_audio.getConstructor(double.class,ResourceLocation.class);
            add_direct=reflected_audio.getMethod("addDirectAirspace",Vec3.class);
            add_shared=reflected_audio.getMethod("addSharedAirspace",Vec3.class,double.class);
            get_shared=reflected_audio.getMethod("getSharedAirspaces");
            Instance raycast_utils=new Instance("com.sonicether.soundphysics.utils.RaycastUtils");
            ray_cast=raycast_utils.getMethod(
                    "rayCast",BlockGetter.class,Vec3.class,Vec3.class,BlockPos.class);
        }
        catch(Throwable ignored){
            return false;
        }
        return true;
    }
    @Override
    public EnvData getEnv(Level level,Player player,Vec3 train_pos){
        EnvData env_data=new EnvData();
        Vec3 player_pos=player.position();
        double distance=train_pos.distanceTo(player_pos);
        if(distance>RemasteredConst.max_process_distance) return env_data;
        try{
            double occlusion=ISPRAccessor.calculateOcclusion(train_pos,player_pos,SoundSource.NEUTRAL,sound_id);
            Instance audio_direction=new Instance(constructor,occlusion,sound_id);
            Vec3 airspace=ISPRAccessor.getSharedAirspace(train_pos,player_pos);
            float[] reflect_ratio=new float[RemasteredConst.ray_bounces];
            if(airspace!=null) audio_direction.invoke(add_direct,airspace);
            float[] send_gains=new float[4],send_cutoffs=new float[4];
            for(int i=0;i<RemasteredConst.ray_count;i++){
                float latitude=(float)Math.asin(2f*i/RemasteredConst.ray_count-1f),longitude=angle*i;
                Vec3 ray_dir=new Vec3(Math.cos(latitude)*Math.cos(longitude),
                        Math.cos(latitude)*Math.sin(longitude),Math.sin(latitude));
                Vec3 ray_end=train_pos.add(ray_dir.scale(far_distance));
                BlockHitResult ray_hit=Instance.invokeStatic(BlockHitResult.class,
                        ray_cast,level,train_pos,ray_end,BlockPos.containing(train_pos));
                if(ray_hit.getType()!=HitResult.Type.BLOCK) continue;
                double total_ray_distance=(float)train_pos.distanceTo(ray_hit.getLocation());
                BlockPos last_hit_block=ray_hit.getBlockPos();
                Vec3 last_ray_dir=ray_dir;
                Vec3 last_hit_pos=ray_hit.getLocation();
                Vec3 last_hit_normal=new Vec3(ray_hit.getDirection().step());
                Vec3 first_shared_airspace=ISPRAccessor.getSharedAirspace(ray_hit,player_pos);
                if(first_shared_airspace!=null)
                    audio_direction.invoke(add_shared,first_shared_airspace,total_ray_distance);
                for(int j=0;j<RemasteredConst.ray_bounces;j++){
                    Vec3 new_ray_dir=ISPRAccessor.reflect(last_ray_dir,last_hit_normal);
                    Vec3 new_ray_start=last_hit_pos;
                    Vec3 new_ray_end=new_ray_start.add(new_ray_dir.scale(far_distance));
                    BlockHitResult new_ray_hit=Instance.invokeStatic(BlockHitResult.class,
                            ray_cast,level,new_ray_start,new_ray_end,last_hit_block);
                    float block_reflectivity=ISPRAccessor.getBlockReflectivity(last_hit_block);
                    float energy=0.25f*(block_reflectivity*0.75f+0.25f);
                    if(new_ray_hit.getType()==HitResult.Type.MISS)
                        total_ray_distance+=last_hit_pos.distanceTo(player_pos);
                    else{
                        Vec3 new_ray_hit_pos=new_ray_hit.getLocation();
                        double new_ray_length=last_hit_pos.distanceTo(new_ray_hit_pos);
                        reflect_ratio[j]+=block_reflectivity;
                        total_ray_distance+=new_ray_length;
                        last_hit_pos=new_ray_hit_pos;
                        last_hit_normal=new Vec3(new_ray_hit.getDirection().step());
                        last_ray_dir=new_ray_dir;
                        last_hit_block=new_ray_hit.getBlockPos();
                        Vec3 shared_airspace=ISPRAccessor.getSharedAirspace(new_ray_hit,player_pos);
                        if(shared_airspace!=null)
                            audio_direction.invoke(add_shared,shared_airspace,total_ray_distance);
                    }
                    if(total_ray_distance>RemasteredConst.decrease_distance){
                        float reflection_delay=(float)Math.max(total_ray_distance,0f)*0.12f*block_reflectivity;
                        for(int k=0;k<4;k++){
                            float value=k==3?reflection_delay-2f:1f-Math.abs(reflection_delay-k);
                            float cross=Math.min(Math.max(value,0f),1f),amp=k==0?6.4f:12.8f;
                            send_gains[k]+=cross*energy*amp*RemasteredConst.d_rays;
                        }
                    }
                    if(new_ray_hit.getType()==HitResult.Type.MISS) break;
                }
            }
            for(int i=0;i<RemasteredConst.ray_bounces;i++) reflect_ratio[i]/=RemasteredConst.ray_count;
            float shared_space=audio_direction.invoke(Integer.class,get_shared)*64f*RemasteredConst.d_rays;
            float avg_space=0f,direct_cutoff=(float)Math.exp(-occlusion*RemasteredConst.block_absorption*3f);
            float send_gain_mul=1f-Math.min((float)(distance/(far_distance*RemasteredConst.reverb_distance)),1f);
            float[] factor={20f,15f,10f,10f},gain_fac={
                    1f,RemasteredConst.ray_bounces>1?reflect_ratio[1]:1f,
                    RemasteredConst.ray_bounces>2?(float)Math.pow(reflect_ratio[2],3.0):1f,
                    RemasteredConst.ray_bounces>3?(float)Math.pow(reflect_ratio[3],4.0):1f};
            for(int i=0;i<4;i++){
                float space_weight=Math.min(Math.max(shared_space/factor[i],0f),1f);
                avg_space+=space_weight;
                send_cutoffs[i]=direct_cutoff*(1f-space_weight)+space_weight;
                if(reflect_ratio.length>i) send_gains[i]*=gain_fac[i];
                send_gains[i]=Math.min(Math.max(i<2?send_gains[i]:(send_gains[i]*1.05f-0.05f),0f),1f);
                send_gains[i]*=(float)Math.pow(send_cutoffs[i],0.1)*send_gain_mul;
                env_data.gains[i]=send_gains[i];
                env_data.cutoffs[i]=send_cutoffs[i];
            }
            avg_space*=0.25f;
            direct_cutoff=Math.max((float)Math.pow(avg_space,0.5)*0.2f,direct_cutoff);
            env_data.gain=(float)Math.pow(direct_cutoff,0.1);
            env_data.cutoff=direct_cutoff;
            env_data.occlusion=occlusion;
            env_data.shared_space=shared_space;
        }
        catch(Throwable ignored){}
        return env_data;
    }
    @Override
    public void handle(double[] mix_buffer,List<TrainData> train_datas){
        for(TrainData train_data:train_datas){
            Arrays.fill(train_buffer,0.0);
            train_data.base_gen.mixTo(train_buffer);
            train_data.vvvf_gen.mixTo(train_buffer);
            train_data.wind_gen.mixTo(train_buffer);
            train_data.setStep(buffer_size);
            EnvData current_env=train_data.current_env;
            for(int i=0;i<buffer_size;i++){
                train_data.addStep();
                double[] cutoffs=current_env.cutoffs;
                double alpha=Math.min(Math.max(current_env.cutoff,0.02),1.0);
                mix_buffer[i]+=train_data.filters[4].process(alpha,train_buffer[i])*current_env.gain;
                for(int j=0;j<4;j++){
                    int tail_ptr=head_ptr+i+RemasteredConst.send_delays[j];
                    if(tail_ptr>=tail_size) tail_ptr-=tail_size;
                    alpha=Math.min(Math.max(cutoffs[j],0.02),1.0);
                    tail_buffers[j][tail_ptr]+=train_data.filters[j].process(alpha,
                            train_buffer[i]*current_env.gains[j]);
                }
            }
        }
        for(int i=0;i<buffer_size;i++){
            double wet=0.0;
            for(int j=0;j<4;j++){
                double current=tail_buffers[j][head_ptr];
                double filtered=filters[j].process(filters_alpha,current);
                tail_buffers[j][head_ptr]=0.0;
                wet+=filtered;
                int tail_ptr=head_ptr+RemasteredConst.send_delays[j];
                if(tail_ptr>=tail_size) tail_ptr-=tail_size;
                tail_buffers[j][tail_ptr]+=filtered*RemasteredConst.send_feedbacks[j];
            }
            mix_buffer[i]+=filters[4].process(filter_alpha,wet);
            head_ptr++;
            if(head_ptr==tail_size) head_ptr=0;
        }
    }
    @Override
    public void reload(){
        far_distance=Configs.far_distance.get();
    }
}