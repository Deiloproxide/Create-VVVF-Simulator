package soundphysics.remastered;
import createvvvfsim.Configs;
import createvvvfsim.EnvData;
import createvvvfsim.TrainData;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import mixin.ISPRAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import soundphysics.Handler;
public class HandlerRemastered extends Handler{
    private static final int buffer_size=Configs.buffer_size;
    private static final double far_distance=Configs.far_distance;
    private static final float angle=(float)(Math.PI*(Math.sqrt(5f)+1f));
    private static ResourceLocation sound_id;
    private static Constructor<?> constructor;
    private static Method add_direct;
    private static Method add_shared;
    private static Method get_shared;
    private static Method ray_cast;
    private static int head_ptr=0;
    private static final int tail_size=buffer_size*64;
    private static final double[] train_buffer=new double[buffer_size];
    private static final double[][] tail_buffers=new double[4][tail_size];
    private static double filter=0.0;
    private static double[] filters=new double[]{0.0,0.0,0.0,0.0};
    public static boolean register(){
        if(!ModList.get().isLoaded("sound_physics_remastered")) return false;
        try{
            sound_id=ResourceLocation.fromNamespaceAndPath(Configs.mod_id,Configs.spr_sound_name);
            Class<?> reflected_audio=Class.forName("com.sonicether.soundphysics.ReflectedAudio");
            constructor=reflected_audio.getConstructor(double.class,ResourceLocation.class);
            add_direct=reflected_audio.getMethod("addDirectAirspace",Vec3.class);
            add_shared=reflected_audio.getMethod("addSharedAirspace",Vec3.class,double.class);
            get_shared=reflected_audio.getMethod("getSharedAirspaces");
            Class<?> raycast_utils=Class.forName("com.sonicether.soundphysics.utils.RaycastUtils");
            ray_cast=raycast_utils.getMethod("rayCast",BlockGetter.class,Vec3.class,Vec3.class,BlockPos.class);
        }
        catch(Throwable ignored){
            return false;
        }
        return true;
    }
    private static Object invoke(Method method,Object instance,Object... args)
            throws InvocationTargetException,IllegalAccessException{
        return method.invoke(instance,args);
    }
    @Override
    public EnvData getEnv(Vec3 train_pos,Vec3 player_pos,Level level){
        EnvData env_data=new EnvData();
        double distance=train_pos.distanceTo(player_pos);
        if(distance>Constants.max_process_distance) return env_data;
        try{
            double occlusion=ISPRAccessor.calculateOcclusion(train_pos,player_pos,SoundSource.NEUTRAL,sound_id);
            Object audio_direction=constructor.newInstance(occlusion,sound_id);
            Vec3 airspace=ISPRAccessor.getSharedAirspace(train_pos,player_pos);
            float[] reflect_ratio=new float[Constants.ray_bounces];
            if(airspace!=null) invoke(add_direct,audio_direction,airspace);
            float[] send_gains=new float[4],send_cutoffs=new float[4];
            for(int i=0;i<Constants.ray_count;i++){
                float latitude=(float)Math.asin(2f*i/Constants.ray_count-1f),longitude=angle*i;
                Vec3 ray_dir=new Vec3(Math.cos(latitude)*Math.cos(longitude),
                        Math.cos(latitude)*Math.sin(longitude),Math.sin(latitude));
                Vec3 ray_end=train_pos.add(ray_dir.scale(far_distance));
                BlockHitResult ray_hit=(BlockHitResult)invoke(
                        ray_cast,null,level,train_pos,ray_end,BlockPos.containing(train_pos));
                if(ray_hit.getType()!=HitResult.Type.BLOCK) continue;
                double total_ray_distance=(float)train_pos.distanceTo(ray_hit.getLocation());
                BlockPos last_hit_block=ray_hit.getBlockPos();
                Vec3 last_ray_dir=ray_dir;
                Vec3 last_hit_pos=ray_hit.getLocation();
                Vec3 last_hit_normal=new Vec3(ray_hit.getDirection().step());
                Vec3 first_shared_airspace=ISPRAccessor.getSharedAirspace(ray_hit,player_pos);
                if(first_shared_airspace!=null)
                    invoke(add_shared,audio_direction,first_shared_airspace,total_ray_distance);
                for(int j=0;j<Constants.ray_bounces;j++){
                    Vec3 new_ray_dir=ISPRAccessor.reflect(last_ray_dir,last_hit_normal);
                    Vec3 new_ray_start=last_hit_pos;
                    Vec3 new_ray_end=new_ray_start.add(new_ray_dir.scale(far_distance));
                    BlockHitResult new_ray_hit=(BlockHitResult)invoke(
                            ray_cast,null,level,new_ray_start,new_ray_end,last_hit_block);
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
                            invoke(add_shared,audio_direction,shared_airspace,total_ray_distance);
                    }
                    if(total_ray_distance>Constants.decrease_distance){
                        float reflection_delay=(float)Math.max(total_ray_distance,0f)*0.12f*block_reflectivity;
                        for(int k=0;k<4;k++){
                            float value=k==3?reflection_delay-2f:1f-Math.abs(reflection_delay-k);
                            float cross=Math.clamp(value,0f,1f),amp=k==0?6.4f:12.8f;
                            send_gains[k]+=cross*energy*amp*Constants.d_rays;
                        }
                    }
                    if(new_ray_hit.getType()==HitResult.Type.MISS) break;
                }
            }
            for(int i=0;i<Constants.ray_bounces;i++) reflect_ratio[i]/=Constants.ray_count;
            float shared_space=(Integer)invoke(get_shared,audio_direction)*64f*Constants.d_rays;
            float avg_space=0f,direct_cutoff=(float)Math.exp(-occlusion*Constants.block_absorption*3f);
            float send_gain_mul=1f-Math.min((float)(distance/(far_distance*Constants.reverb_distance)),1f);
            float[] factor={20f,15f,10f,10f},gain_fac={
                    1f,Constants.ray_bounces>1?reflect_ratio[1]:1f,
                    Constants.ray_bounces>2?(float)Math.pow(reflect_ratio[2],3.0):1f,
                    Constants.ray_bounces>3?(float)Math.pow(reflect_ratio[3],4.0):1f};
            for(int i=0;i<4;i++){
                float space_weight=Math.clamp(shared_space/factor[i],0f,1f);
                avg_space+=space_weight;
                send_cutoffs[i]=direct_cutoff*(1f-space_weight)+space_weight;
                if(reflect_ratio.length>i) send_gains[i]*=gain_fac[i];
                send_gains[i]=Math.clamp(i<2?send_gains[i]:(send_gains[i]*1.05f-0.05f),0f,1f);
                send_gains[i]*=(float)Math.pow(send_cutoffs[i],0.1)*send_gain_mul;
                env_data.sends[i].gain=send_gains[i];
                env_data.sends[i].cutoff=send_cutoffs[i];
            }
            avg_space*=0.25f;
            direct_cutoff=Math.max((float)Math.pow(avg_space,0.5)*0.2f,direct_cutoff);
            env_data.direct_gain=(float)Math.pow(direct_cutoff,0.1);
            env_data.direct_cutoff=direct_cutoff;
            env_data.occlusion=occlusion;
            env_data.shared_space=shared_space;
        }
        catch(Throwable e){
            System.out.println(e.getMessage());
        }
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
                train_data.lowPass(train_buffer[i]);
                mix_buffer[i]+=train_data.filter*current_env.direct_gain;
                for(int j=0;j<4;j++){
                    int tail_ptr=head_ptr+i+Constants.send_delays[j];
                    if(tail_ptr>=tail_size) tail_ptr-=tail_size;
                    tail_buffers[j][tail_ptr]+=train_data.filters[j];
                }
            }
        }
        for(int i=0;i<buffer_size;i++){
            double wet=0.0;
            for(int j=0;j<4;j++){
                double current=tail_buffers[j][head_ptr];
                tail_buffers[j][head_ptr]=0.0;
                filters[j]+=(current-filters[j])*0.82;
                wet+=filters[j];
                int tail_ptr=head_ptr+Constants.send_delays[j];
                if(tail_ptr>=tail_size) tail_ptr-=tail_size;
                tail_buffers[j][tail_ptr]+=filters[j]*Constants.send_feedbacks[j];
            }
            filter+=(wet-filter)*0.92;
            mix_buffer[i]+=filter;
            head_ptr++;
            if(head_ptr==tail_size) head_ptr=0;
        }
    }
}