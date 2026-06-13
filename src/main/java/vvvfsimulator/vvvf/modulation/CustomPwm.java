package vvvfsimulator.vvvf.modulation;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import createvvvfsim.Configs;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseAlternative;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseTypeName;
public class CustomPwm{
    private static final int MAX_PWM_LEVEL=2;
    public byte switchCount=0;
    public double modulationIndexDivision=0.0;
    public double minimumModulationIndex=0.0;
    public long blockCount=0;
    public SwitchEntry[] switchAngleTable=new SwitchEntry[0];
    public byte[] startLevelTable=new byte[0];
    public static class SwitchEntry{
        public double switchAngle;
        public byte output;
        public SwitchEntry(double switchAngle,byte output){
            this.switchAngle=switchAngle;
            this.output=output;
        }
    }
    public CustomPwm(InputStream stream) throws IOException{
        if(stream==null) throw new IOException("Input stream is null");
        switchCount=(byte)readU8(stream);
        modulationIndexDivision=readF64LE(stream);
        minimumModulationIndex=readF64LE(stream);
        blockCount=readU32LE(stream);
        switchAngleTable=new SwitchEntry[Math.toIntExact(blockCount*switchCount)];
        startLevelTable=new byte[Math.toIntExact(blockCount)];
        for(int i=0;i<blockCount;i++){
            startLevelTable[i]=(byte)readU8(stream);
            for(int j=0;j<switchCount;j++){
                byte output=(byte)readU8(stream);
                double switchAngle=readF64LE(stream);
                switchAngleTable[i*switchCount+j]=new SwitchEntry(switchAngle,output);
            }
        }
    }
    public int getPwm(double m,double x){
        int index=Math.clamp((int)((m-minimumModulationIndex)/modulationIndexDivision),0,(int)blockCount-1);
        SwitchEntry[] alpha=new SwitchEntry[switchCount];
        byte startLevel=startLevelTable[index];
        System.arraycopy(switchAngleTable,index*switchCount,alpha,0,switchCount);
        return getPwm(alpha,x,startLevel);
    }
    public static int getPwm(SwitchEntry[] alpha,double x,byte startLevel){
        x%=MyMath.M_2PI;
        int orthant=(int)(x/MyMath.M_PI_2);
        double angle=x%MyMath.M_PI_2;
        if((orthant&1)==1) angle=MyMath.M_PI_2-angle;
        int pwm=startLevel;
        for(SwitchEntry switchEntry: alpha){
            if(switchEntry.switchAngle<=angle) pwm=switchEntry.output;
            else break;
        }
        if(orthant>1) pwm=MAX_PWM_LEVEL-pwm;
        return pwm;
    }
    private static int readU8(InputStream stream) throws IOException{
        int value=stream.read();
        if(value<0) throw new IOException("Unexpected EOF");
        return value;
    }
    private static long readU32LE(InputStream stream) throws IOException{
        long b0=readU8(stream), b1=readU8(stream), b2=readU8(stream), b3=readU8(stream);
        return b0|(b1<<8)|(b2<<16)|(b3<<24);
    }
    private static double readF64LE(InputStream stream) throws IOException{
        long value=0;
        for(int i=0;i<8;i++) value|=((long)readU8(stream))<<(8*i);
        return Double.longBitsToDouble(value);
    }
    public static final class CustomPwmPresets{
        private static final Map<Key,CustomPwm> PRESETS=new HashMap<>();
        private static final AtomicBoolean LOADED=new AtomicBoolean(false);
        private static final String BASE_PATH=Configs.table;
        public static void register(int level,PulseTypeName pulseType,
                                    int pulseCount,PulseAlternative alternative,CustomPwm pwm){
            PRESETS.put(new Key(level,pulseType,pulseCount,alternative),pwm);
        }
        public static void preload(){
            ensureLoaded();
        }
        public static CustomPwm getCustomPwm(int level,PulseTypeName pulseType,
                                             int pulseCount,PulseAlternative alternative){
            ensureLoaded();
            return PRESETS.get(new Key(level,pulseType,pulseCount,alternative));
        }
        private static void ensureLoaded(){
            if(LOADED.get()) return;
            synchronized(PRESETS){
                if(LOADED.get()) return;
                loadFromResources();
                LOADED.set(true);
            }
        }
        private static void loadFromResources(){
            for(int level: new int[]{2,3}){
                loadType(level,"Chm",PulseTypeName.CHM);
                loadType(level,"She",PulseTypeName.SHE);
            }
        }
        private static void loadType(int level,String typeTag,PulseTypeName pulseType){
            for(int pulseCount=1;pulseCount<=25;pulseCount++){
                tryLoad(level,typeTag,pulseType,pulseCount,PulseAlternative.Default,"Default");
                for(int alt=1;alt<=30;alt++){
                    PulseAlternative alternative=alternativeFromAltNumber(alt);
                    if(alternative==null) break;
                    tryLoad(level,typeTag,pulseType,pulseCount,alternative,"Alt"+alt);
                }
            }
        }
        private static PulseAlternative alternativeFromAltNumber(int altNumber){
            int ordinal=PulseAlternative.Alt1.ordinal()+(altNumber-1);
            PulseAlternative[] values=PulseAlternative.values();
            if(ordinal<0 || ordinal>=values.length) return null;
            return values[ordinal];
        }
        private static void tryLoad(int level,String typeTag,PulseTypeName pulseType,
                                    int pulseCount,PulseAlternative alternative,String variantTag){
            String fileName="L"+level+typeTag+pulseCount+variantTag+".bin";
            String resourcePath=BASE_PATH+fileName;
            try(InputStream stream=CustomPwm.class.getResourceAsStream(resourcePath)){
                if(stream==null) return;
                CustomPwm pwm=new CustomPwm(stream);
                register(level,pulseType,pulseCount,alternative,pwm);
            }
            catch(IOException e){
                throw new RuntimeException("Failed to load custom PWM preset: "+resourcePath,e);
            }
        }
        private record Key(int level,PulseTypeName pulseType,int pulseCount,PulseAlternative alternative){}
    }
}