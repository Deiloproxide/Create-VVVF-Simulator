package vvvfsimulator.data.vvvf;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.ComposerException;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.scanner.ScannerException;
import vvvfsimulator.data.vvvf.Struct.AmplitudeValue;
import vvvfsimulator.data.vvvf.Struct.AsyncControlEx;
import vvvfsimulator.data.vvvf.Struct.AsyncControlEx.CarrierFrequencyEx.TableValue.Parameter;
import vvvfsimulator.data.vvvf.Struct.AsyncControlEx.CarrierFrequencyEx.VibratoValueEx;
import vvvfsimulator.data.vvvf.Struct.AsyncControlEx.RandomModulationEx.ParameterEx;
import vvvfsimulator.data.vvvf.Struct.FunctionValue;
import vvvfsimulator.data.vvvf.Struct.FunctionValue.FunctionType;
import vvvfsimulator.data.vvvf.Struct.FreqAmp;
import vvvfsimulator.data.vvvf.Struct.PulseControlEx;
import vvvfsimulator.vvvf.model.Config;
import vvvfsimulator.vvvf.model.Struct.JerkSettings;
import vvvfsimulator.vvvf.model.Struct.JerkSettings.JerkInfo;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.CarrierFrequency;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.CarrierFrequency.VibratoValue.BaseWaveType;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.RandomModulation;
import vvvfsimulator.vvvf.model.Struct.PulseControl.AsyncControl.RandomModulation.Parameter.ValueMode;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataKey;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseDataValue;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseHarmonic;
import vvvfsimulator.vvvf.model.Struct.PulseControl.Pulse.PulseTypeName;
import yamlloader.LoadContext;
import yamlloader.LoadException;
public class Manager{
    private static final Struct TEMPLATE=new Struct();
    public static volatile Struct loadData;
    public static volatile Struct current=deepClone(TEMPLATE);
    public static volatile String loadPath="";
    public static boolean save(String path,Struct data,boolean useException){
        try(Writer writer=Files.newBufferedWriter(Path.of(path),StandardCharsets.UTF_8)){
            createYaml().dump(toYaml(data),writer);
            return true;
        }
        catch(Exception e){
            if(useException) throw new IllegalStateException("Failed to save VVVF yaml: "+path,e);
            return false;
        }
    }
    public static boolean save(String path,Struct data){
        return save(path,data,false);
    }
    private static LoadContext createContext(LoadException exception,Mark mark){
        if(mark==null) return new LoadContext(exception,0,0);
        return new LoadContext(exception,mark.getLine(),mark.getColumn());
    }
    public static LoadContext load(String path,InputStream inputStream){
        YamlStruct yamlStruct=null;
        LoadContext context=null;
        try(Reader reader=new InputStreamReader(inputStream,StandardCharsets.UTF_8)){
            try{
                yamlStruct=createYaml().loadAs(reader,YamlStruct.class);
                reader.close();
            }
            catch(ScannerException e){
                context=createContext(LoadException.lex,e.getProblemMark());
            }
            catch(ParserException e){
                context=createContext(LoadException.parse,e.getProblemMark());
            }
            catch(ComposerException e){
                context=createContext(LoadException.compose,e.getProblemMark());
            }
            catch(DuplicateKeyException e){
                context=createContext(LoadException.dump,e.getProblemMark());
            }
            catch(ConstructorException e){
                context=createContext(LoadException.init,e.getProblemMark());
            }
            catch(YAMLException e){
                if(e.getCause() instanceof IOException) context=new LoadContext(LoadException.io,0,0);
                else context=new LoadContext(LoadException.init,0,0);
            }
        }
        catch(IOException e){
            context=new LoadContext(LoadException.io,0,0);
        }
        if(context!=null) return context;
        if(yamlStruct==null) return new LoadContext(LoadException.empty,0,0);
        try{
            current=fromYaml(yamlStruct);
        }
        catch(RuntimeException e){
            return new LoadContext(LoadException.init,0,0);
        }
        loadPath=path;
        loadData=deepClone(current);
        return new LoadContext(LoadException.normal,0,0);
    }
    public static boolean saveCurrent(String path){
        boolean result=save(path,current);
        if(result){
            loadPath=path;
            loadData=deepClone(current);
        }
        return result;
    }
    public static Struct deepClone(Struct source){
        Struct copy=new Struct();
        copy.level=source.level;
        copy.jerkSetting=copyJerkSettings(source.jerkSetting);
        copy.minimumFrequency.accelerating=source.minimumFrequency.accelerating;
        copy.minimumFrequency.braking=source.minimumFrequency.braking;
        for(PulseControlEx ex: source.acceleratePattern) copy.acceleratePattern.add(ex.copyEx());
        for(PulseControlEx ex: source.brakingPattern) copy.brakingPattern.add(ex.copyEx());
        copy.sortForRuntime();
        return copy;
    }
    public static Struct getTemplate(){
        return deepClone(TEMPLATE);
    }
    public static void resetCurrent(){
        current=deepClone(TEMPLATE);
        loadPath="";
        loadData=null;
    }
    public static boolean isCurrentEquivalent(Struct other){
        return other!=null && createYaml().dump(toYaml(current)).equals(createYaml().dump(toYaml(other)));
    }
    public static String getLoadedYamlName(){
        if(loadPath==null || loadPath.isEmpty()) return "";
        Path fileName=Path.of(loadPath).getFileName();
        String name=fileName==null?"":fileName.toString();
        int dot=name.lastIndexOf('.');
        return dot<=0?name:name.substring(0,dot);
    }
    private static Yaml createYaml(){
        LoaderOptions loaderOptions=new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        loaderOptions.setMaxAliasesForCollections(1024);
        Constructor constructor=new Constructor(YamlStruct.class,loaderOptions);
        DumperOptions dumperOptions=new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        Representer representer=new Representer(dumperOptions);
        addClassTags(representer);
        Yaml yaml=new Yaml(constructor,representer,dumperOptions);
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml;
    }
    private static void addClassTags(Representer representer){
        Class<?>[] classes={YamlStruct.class,YamlJerkSettings.class,YamlJerk.class,
                YamlJerkInfo.class,YamlMinimumBaseFrequency.class,YamlPulseControl.class,
                YamlFunctionValue.class,YamlPulse.class,YamlDiscreteTimeConfiguration.class,
                YamlPulseHarmonic.class,YamlCarrierWaveConfiguration.class,YamlPulseDataValue.class,
                YamlAsyncControl.class,YamlRandomModulation.class,YamlRandomParameter.class,
                YamlCarrierFrequency.class,YamlVibratoValue.class,YamlVibratoParameter.class,
                YamlCarrierFrequencyTable.class,YamlCarrierFrequencyTableParameter.class,
                YamlAmplitudeValue.class,YamlAmplitudeParameter.class};
        for(Class<?> clazz: classes) representer.addClassTag(clazz,Tag.MAP);
    }
    private static Struct fromYaml(YamlStruct yaml){
        Struct result=new Struct();
        result.level=yaml.Level;
        result.jerkSetting=fromYaml(yaml.JerkSetting);
        result.minimumFrequency.accelerating=yaml.MinimumFrequency.Accelerating;
        result.minimumFrequency.braking=yaml.MinimumFrequency.Braking;
        for(YamlPulseControl control:nonNull(yaml.AcceleratePattern))
            result.acceleratePattern.add(fromYaml(control,result.level));
        for(YamlPulseControl control:nonNull(yaml.BrakingPattern))
            result.brakingPattern.add(fromYaml(control,result.level));
        result.sortForRuntime();
        return result;
    }
    private static JerkSettings fromYaml(YamlJerkSettings yaml){
        JerkSettings result=new JerkSettings();
        copyJerkInfo(yaml.Accelerating.On,result.accelerating.on);
        copyJerkInfo(yaml.Accelerating.Off,result.accelerating.off);
        copyJerkInfo(yaml.Braking.On,result.braking.on);
        copyJerkInfo(yaml.Braking.Off,result.braking.off);
        return result;
    }
    private static PulseControlEx fromYaml(YamlPulseControl yaml,int level){
        PulseControlEx result=new PulseControlEx();
        result.controlFrequencyFrom=yaml.ControlFrequencyFrom;
        result.rotateFrequencyFrom=yaml.RotateFrequencyFrom;
        result.rotateFrequencyBelow=yaml.RotateFrequencyBelow;
        result.enableFreeRunOn=yaml.EnableFreeRunOn;
        result.stuckFreeRunOn=yaml.StuckFreeRunOn;
        result.enableFreeRunOff=yaml.EnableFreeRunOff;
        result.stuckFreeRunOff=yaml.StuckFreeRunOff;
        result.enableNormal=yaml.EnableNormal;
        result.pulseMode=fromYaml(yaml.PulseMode,level);
        result.amplitude=fromYaml(yaml.Amplitude);
        result.asyncModulationDataEx=fromYamlEx(yaml.AsyncModulationData);
        result.asyncModulationData=fromYamlBase(yaml.AsyncModulationData);
        return result;
    }
    private static Pulse fromYaml(YamlPulse yaml,int level){
        Pulse result=new Pulse();
        result.pulseType=enumValue(Pulse.PulseTypeName.class,yaml.PulseType,result.pulseType);
        result.pulseCount=yaml.PulseCount;
        result.alternative=enumValue(Pulse.PulseAlternative.class,yaml.Alternative,result.alternative);
        result.discreteTime.enabled=yaml.DiscreteTime.Enabled;
        result.discreteTime.steps=yaml.DiscreteTime.Steps;
        result.discreteTime.mode=enumValue(Pulse.DiscreteTimeConfiguration.DiscreteTimeMode.class,
                yaml.DiscreteTime.Mode,result.discreteTime.mode);
        result.baseWave=enumValue(Pulse.BaseWaveType.class,yaml.BaseWave,result.baseWave);
        result.pulseHarmonics=new ArrayList<>();
        for(YamlPulseHarmonic harmonic:nonNull(yaml.PulseHarmonics)){
            Pulse.PulseHarmonic out=new Pulse.PulseHarmonic();
            out.harmonic=harmonic.Harmonic;
            out.isHarmonicProportional=harmonic.IsHarmonicProportional;
            out.amplitude=harmonic.Amplitude;
            out.isAmplitudeProportional=harmonic.IsAmplitudeProportional;
            out.initialPhase=harmonic.InitialPhase;
            out.type=enumValue(Pulse.PulseHarmonic.PulseHarmonicType.class,harmonic.Type,out.type);
            result.pulseHarmonics.add(out);
        }
        result.carrierWave.type=enumValue(Pulse.CarrierWaveConfiguration.CarrierWaveType.class,
                yaml.CarrierWave.Type,result.carrierWave.type);
        result.carrierWave.option=enumValue(Pulse.CarrierWaveConfiguration.CarrierWaveOption.class,
                yaml.CarrierWave.Option,result.carrierWave.option);
        result.pulseData=new HashMap<>();
        for(Map.Entry<String,YamlPulseDataValue> entry:nonNull(yaml.PulseData).entrySet()){
            PulseDataKey key=enumValue(PulseDataKey.class,entry.getKey(),null);
            if(key==null) continue;
            YamlPulseDataValue value=entry.getValue()==null?new YamlPulseDataValue():entry.getValue();
            PulseDataValue out=new PulseDataValue();
            out.mode=enumValue(PulseDataValue.PulseDataValueMode.class,value.Mode,out.mode);
            out.constant=value.Constant;
            result.pulseData.put(key,out);
        }
        for(PulseDataKey key: Config.getAvailablePulseDataKey(result,level))
            result.pulseData.putIfAbsent(key,new PulseDataValue());
        return result;
    }
    private static AsyncControlEx fromYamlEx(YamlAsyncControl yaml){
        AsyncControlEx result=new AsyncControlEx();
        result.randomData.range=fromYaml(yaml.RandomData.Range);
        result.randomData.interval=fromYaml(yaml.RandomData.Interval);
        result.carrierWaveData.mode=enumValue(CarrierFrequency.ValueMode.class,
                yaml.CarrierWaveData.Mode,result.carrierWaveData.mode);
        result.carrierWaveData.constant=yaml.CarrierWaveData.Constant;
        result.carrierWaveData.movingValue=fromYaml(yaml.CarrierWaveData.MovingValue);
        result.carrierWaveData.vibratoData.highest=fromYaml(yaml.CarrierWaveData.VibratoData.Highest);
        result.carrierWaveData.vibratoData.lowest=fromYaml(yaml.CarrierWaveData.VibratoData.Lowest);
        result.carrierWaveData.vibratoData.interval=fromYaml(yaml.CarrierWaveData.VibratoData.Interval);
        result.carrierWaveData.vibratoData.baseWave=enumValue(BaseWaveType.class,
                yaml.CarrierWaveData.VibratoData.BaseWave,result.carrierWaveData.vibratoData.baseWave);
        result.carrierWaveData.carrierFrequencyTable.table=new ArrayList<>();
        for(YamlCarrierFrequencyTableParameter parameter:nonNull(yaml.CarrierWaveData.CarrierFrequencyTable.Table)){
            Parameter out=new Parameter();
            out.controlFrequencyFrom=parameter.ControlFrequencyFrom;
            out.carrierFrequency=parameter.CarrierFrequency;
            out.freeRunStuckAtHere=parameter.FreeRunStuckAtHere;
            result.carrierWaveData.carrierFrequencyTable.table.add(out);
        }
        return result;
    }
    private static AsyncControl fromYamlBase(YamlAsyncControl yaml){
        AsyncControl result=new AsyncControl();
        result.randomData.range=fromYamlBase(yaml.RandomData.Range);
        result.randomData.interval=fromYamlBase(yaml.RandomData.Interval);
        result.carrierWaveData.mode=enumValue(CarrierFrequency.ValueMode.class,
                yaml.CarrierWaveData.Mode,result.carrierWaveData.mode);
        result.carrierWaveData.constant=yaml.CarrierWaveData.Constant;
        result.carrierWaveData.vibratoData.baseWave=enumValue(BaseWaveType.class,
                yaml.CarrierWaveData.VibratoData.BaseWave,result.carrierWaveData.vibratoData.baseWave);
        return result;
    }
    private static ParameterEx fromYaml(YamlRandomParameter yaml){
        ParameterEx result=new ParameterEx();
        result.mode=enumValue(ValueMode.class,yaml.Mode,result.mode);
        result.constant=yaml.Constant;
        result.movingValue=fromYaml(yaml.MovingValue);
        return result;
    }
    private static RandomModulation.Parameter fromYamlBase(YamlRandomParameter yaml){
        RandomModulation.Parameter result=new RandomModulation.Parameter();
        result.mode=enumValue(ValueMode.class,yaml.Mode,result.mode);
        result.constant=yaml.Constant;
        return result;
    }
    private static VibratoValueEx.ParameterEx fromYaml(YamlVibratoParameter yaml){
        VibratoValueEx.ParameterEx result=new VibratoValueEx.ParameterEx();
        result.mode=enumValue(VibratoValueEx.ParameterEx.ValueMode.class,
                yaml.Mode,result.mode);
        result.constant=yaml.Constant;
        result.movingValue=fromYaml(yaml.MovingValue);
        return result;
    }
    private static FunctionValue fromYaml(YamlFunctionValue yaml){
        FunctionValue result=new FunctionValue();
        result.type=enumValue(FunctionType.class,yaml.Type,result.type);
        result.start=yaml.Start;
        result.startValue=yaml.StartValue;
        result.end=yaml.End;
        result.endValue=yaml.EndValue;
        result.degree=yaml.Degree;
        result.curveRate=yaml.CurveRate;
        return result;
    }
    private static AmplitudeValue fromYaml(YamlAmplitudeValue yaml){
        AmplitudeValue result=new AmplitudeValue();
        result.defaultValue=fromYaml(yaml.Default);
        result.powerOn=fromYaml(yaml.PowerOn);
        result.powerOff=fromYaml(yaml.PowerOff);
        return result;
    }
    private static AmplitudeValue.Parameter fromYaml(YamlAmplitudeParameter yaml){
        AmplitudeValue.Parameter result=new AmplitudeValue.Parameter();
        result.mode=enumValue(AmplitudeValue.Parameter.ValueMode.class,yaml.Mode,result.mode);
        result.startFrequency=yaml.StartFrequency;
        result.startAmplitude=yaml.StartAmplitude;
        result.endFrequency=yaml.EndFrequency;
        result.endAmplitude=yaml.EndAmplitude;
        result.curveChangeRate=yaml.CurveChangeRate;
        result.cutOffAmplitude=yaml.CutOffAmplitude;
        result.maxAmplitude=yaml.MaxAmplitude;
        result.disableRangeLimit=yaml.DisableRangeLimit;
        result.polynomial=yaml.Polynomial;
        result.amplitudeTableInterpolation=yaml.AmplitudeTableInterpolation;
        result.amplitudeTable=toFreqAmpArray(yaml.AmplitudeTable);
        return result;
    }
    private static FreqAmp[] toFreqAmpArray(List<Object> source){
        if(source==null || source.isEmpty()) return new FreqAmp[0];
        List<FreqAmp> result=new ArrayList<>();
        for(Object item: source){
            FreqAmp freqAmp=toFreqAmp(item);
            if(freqAmp!=null) result.add(freqAmp);
        }
        return result.toArray(FreqAmp[]::new);
    }
    private static FreqAmp toFreqAmp(Object item){
        if(item instanceof Map<?,?> map){
            double frequency=number(map.get("Frequency"),number(map.get("Item1"),0));
            double amplitude=number(map.get("Amplitude"),number(map.get("Item2"),0));
            return new FreqAmp(frequency,amplitude);
        }
        if(item instanceof List<?> list && list.size()>=2)
            return new FreqAmp(number(list.get(0),0),number(list.get(1),0));
        return null;
    }
    private static YamlStruct toYaml(Struct source){
        YamlStruct result=new YamlStruct();
        result.Level=source.level;
        result.JerkSetting=toYaml(source.jerkSetting);
        result.MinimumFrequency.Accelerating=source.minimumFrequency.accelerating;
        result.MinimumFrequency.Braking=source.minimumFrequency.braking;
        for(PulseControlEx control:source.acceleratePattern) result.AcceleratePattern.add(toYaml(control));
        for(PulseControlEx control:source.brakingPattern) result.BrakingPattern.add(toYaml(control));
        return result;
    }
    private static YamlJerkSettings toYaml(JerkSettings source){
        YamlJerkSettings result=new YamlJerkSettings();
        copyJerkInfo(source.accelerating.on,result.Accelerating.On);
        copyJerkInfo(source.accelerating.off,result.Accelerating.Off);
        copyJerkInfo(source.braking.on,result.Braking.On);
        copyJerkInfo(source.braking.off,result.Braking.Off);
        return result;
    }
    private static YamlPulseControl toYaml(PulseControlEx source){
        YamlPulseControl result=new YamlPulseControl();
        result.ControlFrequencyFrom=source.controlFrequencyFrom;
        result.RotateFrequencyFrom=source.rotateFrequencyFrom;
        result.RotateFrequencyBelow=source.rotateFrequencyBelow;
        result.EnableFreeRunOn=source.enableFreeRunOn;
        result.StuckFreeRunOn=source.stuckFreeRunOn;
        result.EnableFreeRunOff=source.enableFreeRunOff;
        result.StuckFreeRunOff=source.stuckFreeRunOff;
        result.EnableNormal=source.enableNormal;
        result.PulseMode=toYaml(source.pulseMode);
        result.Amplitude=toYaml(source.amplitude);
        result.AsyncModulationData=toYaml(source.asyncModulationDataEx);
        return result;
    }
    private static YamlPulse toYaml(Pulse source){
        YamlPulse result=new YamlPulse();
        result.PulseType=enumName(source.pulseType);
        result.PulseCount=source.pulseCount;
        result.Alternative=enumName(source.alternative);
        result.DiscreteTime.Enabled=source.discreteTime.enabled;
        result.DiscreteTime.Steps=source.discreteTime.steps;
        result.DiscreteTime.Mode=enumName(source.discreteTime.mode);
        result.BaseWave=enumName(source.baseWave);
        result.PulseHarmonics=new ArrayList<>();
        for(PulseHarmonic harmonic:source.pulseHarmonics){
            YamlPulseHarmonic out=new YamlPulseHarmonic();
            out.Harmonic=harmonic.harmonic;
            out.IsHarmonicProportional=harmonic.isHarmonicProportional;
            out.Amplitude=harmonic.amplitude;
            out.IsAmplitudeProportional=harmonic.isAmplitudeProportional;
            out.InitialPhase=harmonic.initialPhase;
            out.Type=enumName(harmonic.type);
            result.PulseHarmonics.add(out);
        }
        result.CarrierWave.Type=enumName(source.carrierWave.type);
        result.CarrierWave.Option=enumName(source.carrierWave.option);
        result.PulseData=new HashMap<>();
        for(Map.Entry<PulseDataKey,PulseDataValue> entry:source.pulseData.entrySet()){
            YamlPulseDataValue out=new YamlPulseDataValue();
            out.Mode=enumName(entry.getValue().mode);
            out.Constant=entry.getValue().constant;
            result.PulseData.put(enumName(entry.getKey()),out);
        }
        return result;
    }
    private static YamlAsyncControl toYaml(AsyncControlEx source){
        YamlAsyncControl result=new YamlAsyncControl();
        result.RandomData.Range=toYaml(source.randomData.range);
        result.RandomData.Interval=toYaml(source.randomData.interval);
        result.CarrierWaveData.Mode=enumName(source.carrierWaveData.mode);
        result.CarrierWaveData.Constant=source.carrierWaveData.constant;
        result.CarrierWaveData.MovingValue=toYaml(source.carrierWaveData.movingValue);
        result.CarrierWaveData.VibratoData.Highest=toYaml(source.carrierWaveData.vibratoData.highest);
        result.CarrierWaveData.VibratoData.Lowest=toYaml(source.carrierWaveData.vibratoData.lowest);
        result.CarrierWaveData.VibratoData.Interval=toYaml(source.carrierWaveData.vibratoData.interval);
        result.CarrierWaveData.VibratoData.BaseWave=enumName(source.carrierWaveData.vibratoData.baseWave);
        result.CarrierWaveData.CarrierFrequencyTable.Table=new ArrayList<>();
        for(Parameter parameter:source.carrierWaveData.carrierFrequencyTable.table){
            YamlCarrierFrequencyTableParameter out=new YamlCarrierFrequencyTableParameter();
            out.ControlFrequencyFrom=parameter.controlFrequencyFrom;
            out.CarrierFrequency=parameter.carrierFrequency;
            out.FreeRunStuckAtHere=parameter.freeRunStuckAtHere;
            result.CarrierWaveData.CarrierFrequencyTable.Table.add(out);
        }
        return result;
    }
    private static YamlRandomParameter toYaml(ParameterEx source){
        YamlRandomParameter result=new YamlRandomParameter();
        result.Mode=enumName(source.mode);
        result.Constant=source.constant;
        result.MovingValue=toYaml(source.movingValue);
        return result;
    }
    private static YamlVibratoParameter toYaml(VibratoValueEx.ParameterEx source){
        YamlVibratoParameter result=new YamlVibratoParameter();
        result.Mode=enumName(source.mode);
        result.Constant=source.constant;
        result.MovingValue=toYaml(source.movingValue);
        return result;
    }
    private static YamlFunctionValue toYaml(FunctionValue source){
        YamlFunctionValue result=new YamlFunctionValue();
        result.Type=enumName(source.type);
        result.Start=source.start;
        result.StartValue=source.startValue;
        result.End=source.end;
        result.EndValue=source.endValue;
        result.Degree=source.degree;
        result.CurveRate=source.curveRate;
        return result;
    }
    private static YamlAmplitudeValue toYaml(AmplitudeValue source){
        YamlAmplitudeValue result=new YamlAmplitudeValue();
        result.Default=toYaml(source.defaultValue);
        result.PowerOn=toYaml(source.powerOn);
        result.PowerOff=toYaml(source.powerOff);
        return result;
    }
    private static YamlAmplitudeParameter toYaml(AmplitudeValue.Parameter source){
        YamlAmplitudeParameter result=new YamlAmplitudeParameter();
        result.Mode=enumName(source.mode);
        result.StartFrequency=source.startFrequency;
        result.StartAmplitude=source.startAmplitude;
        result.EndFrequency=source.endFrequency;
        result.EndAmplitude=source.endAmplitude;
        result.CurveChangeRate=source.curveChangeRate;
        result.CutOffAmplitude=source.cutOffAmplitude;
        result.MaxAmplitude=source.maxAmplitude;
        result.DisableRangeLimit=source.disableRangeLimit;
        result.Polynomial=source.polynomial;
        result.AmplitudeTableInterpolation=source.amplitudeTableInterpolation;
        result.AmplitudeTable=new ArrayList<>();
        for(FreqAmp freqAmp: source.amplitudeTable){
            Map<String,Double> out=new HashMap<>();
            out.put("Frequency",freqAmp.frequency);
            out.put("Amplitude",freqAmp.amplitude);
            result.AmplitudeTable.add(out);
        }
        return result;
    }
    private static JerkSettings copyJerkSettings(JerkSettings source){
        JerkSettings copy=new JerkSettings();
        copyJerkInfo(source.accelerating.on,copy.accelerating.on);
        copyJerkInfo(source.accelerating.off,copy.accelerating.off);
        copyJerkInfo(source.braking.on,copy.braking.on);
        copyJerkInfo(source.braking.off,copy.braking.off);
        return copy;
    }
    private static void copyJerkInfo(YamlJerkInfo source,JerkInfo target){
        target.frequencyChangeRate=source.FrequencyChangeRate;
        target.maxControlFrequency=source.MaxControlFrequency;
    }
    private static void copyJerkInfo(JerkInfo source,YamlJerkInfo target){
        target.FrequencyChangeRate=source.frequencyChangeRate;
        target.MaxControlFrequency=source.maxControlFrequency;
    }
    private static void copyJerkInfo(JerkInfo source,JerkInfo target){
        target.frequencyChangeRate=source.frequencyChangeRate;
        target.maxControlFrequency=source.maxControlFrequency;
    }
    private static <T> List<T> nonNull(List<T> list){
        return list==null?List.of():list;
    }
    private static <K,V> Map<K,V> nonNull(Map<K,V> map){
        return map==null?Map.of():map;
    }
    private static double number(Object value,double fallback){
        return value instanceof Number number?number.doubleValue():fallback;
    }
    private static <E extends Enum<E>> E enumValue(Class<E> type,String value,E fallback){
        if(value==null) return fallback;
        String fixed=value.trim();
        if(type==PulseTypeName.class && fixed.equals("ΔΣ")) fixed="DELTA_SIGMA";
        try{
            return Enum.valueOf(type,fixed);
        }
        catch(IllegalArgumentException e){
            return fallback;
        }
    }
    private static String enumName(Enum<?> value){
        if(value==null) return null;
        if(value==PulseTypeName.DELTA_SIGMA) return "ΔΣ";
        return value.name();
    }
    public static class YamlStruct{
        public int Level=2;
        public YamlJerkSettings JerkSetting=new YamlJerkSettings();
        public YamlMinimumBaseFrequency MinimumFrequency=new YamlMinimumBaseFrequency();
        public List<YamlPulseControl> AcceleratePattern=new ArrayList<>();
        public List<YamlPulseControl> BrakingPattern=new ArrayList<>();
    }
    public static class YamlJerkSettings{
        public YamlJerk Braking=new YamlJerk();
        public YamlJerk Accelerating=new YamlJerk();
    }
    public static class YamlJerk{
        public YamlJerkInfo On=new YamlJerkInfo();
        public YamlJerkInfo Off=new YamlJerkInfo();
    }
    public static class YamlJerkInfo{
        public double FrequencyChangeRate=60;
        public double MaxControlFrequency=60;
    }
    public static class YamlMinimumBaseFrequency{
        public double Accelerating=-1.0;
        public double Braking=-1.0;
    }
    public static class YamlPulseControl{
        public double ControlFrequencyFrom=-1;
        public double RotateFrequencyFrom=-1;
        public double RotateFrequencyBelow=-1;
        public boolean EnableFreeRunOn=true;
        public boolean StuckFreeRunOn=false;
        public boolean EnableFreeRunOff=true;
        public boolean StuckFreeRunOff=false;
        public boolean EnableNormal=true;
        public YamlPulse PulseMode=new YamlPulse();
        public YamlAmplitudeValue Amplitude=new YamlAmplitudeValue();
        public YamlAsyncControl AsyncModulationData=new YamlAsyncControl();
    }
    public static class YamlFunctionValue{
        public String Type="Proportional";
        public double Start=0;
        public double StartValue=0;
        public double End=1;
        public double EndValue=100;
        public double Degree=2;
        public double CurveRate=0;
    }
    public static class YamlPulse{
        public String PulseType="ASYNC";
        public int PulseCount=1;
        public String Alternative="Default";
        public YamlDiscreteTimeConfiguration DiscreteTime=new YamlDiscreteTimeConfiguration();
        public String BaseWave="Sine";
        public List<YamlPulseHarmonic> PulseHarmonics=new ArrayList<>();
        public YamlCarrierWaveConfiguration CarrierWave=new YamlCarrierWaveConfiguration();
        public Map<String,YamlPulseDataValue> PulseData=new HashMap<>();
    }
    public static class YamlDiscreteTimeConfiguration{
        public boolean Enabled=false;
        public int Steps=2;
        public String Mode="Middle";
    }
    public static class YamlPulseHarmonic{
        public double Harmonic=3;
        public boolean IsHarmonicProportional=true;
        public double Amplitude=0.2;
        public boolean IsAmplitudeProportional=true;
        public double InitialPhase=0;
        public String Type="Sine";
    }
    public static class YamlCarrierWaveConfiguration{
        public String Type="Triangle";
        public String Option="FallStart";
    }
    public static class YamlPulseDataValue{
        public String Mode="Const";
        public double Constant=-1;
        public YamlFunctionValue MovingValue=new YamlFunctionValue();
    }
    public static class YamlAsyncControl{
        public YamlRandomModulation RandomData=new YamlRandomModulation();
        public YamlCarrierFrequency CarrierWaveData=new YamlCarrierFrequency();
    }
    public static class YamlRandomModulation{
        public YamlRandomParameter Range=new YamlRandomParameter();
        public YamlRandomParameter Interval=new YamlRandomParameter();
    }
    public static class YamlRandomParameter{
        public String Mode="Const";
        public double Constant=0;
        public YamlFunctionValue MovingValue=new YamlFunctionValue();
    }
    public static class YamlCarrierFrequency{
        public String Mode="Const";
        public double Constant=-1.0;
        public YamlFunctionValue MovingValue=new YamlFunctionValue();
        public YamlVibratoValue VibratoData=new YamlVibratoValue();
        public YamlCarrierFrequencyTable CarrierFrequencyTable=new YamlCarrierFrequencyTable();
    }
    public static class YamlVibratoValue{
        public YamlVibratoParameter Highest=new YamlVibratoParameter();
        public YamlVibratoParameter Lowest=new YamlVibratoParameter();
        public YamlVibratoParameter Interval=new YamlVibratoParameter();
        public String BaseWave="Triangle";
    }
    public static class YamlVibratoParameter{
        public String Mode="Const";
        public double Constant=-1;
        public YamlFunctionValue MovingValue=new YamlFunctionValue();
    }
    public static class YamlCarrierFrequencyTable{
        public List<YamlCarrierFrequencyTableParameter> Table=new ArrayList<>();
    }
    public static class YamlCarrierFrequencyTableParameter{
        public double ControlFrequencyFrom=-1;
        public double CarrierFrequency=1000;
        public boolean FreeRunStuckAtHere=false;
    }
    public static class YamlAmplitudeValue{
        public YamlAmplitudeParameter Default=new YamlAmplitudeParameter();
        public YamlAmplitudeParameter PowerOn=new YamlAmplitudeParameter();
        public YamlAmplitudeParameter PowerOff=new YamlAmplitudeParameter();
    }
    public static class YamlAmplitudeParameter{
        public String Mode="Linear";
        public double StartFrequency=-1;
        public double StartAmplitude=-1;
        public double EndFrequency=-1;
        public double EndAmplitude=-1;
        public double CurveChangeRate=0;
        public double CutOffAmplitude=-1;
        public double MaxAmplitude=-1;
        public boolean DisableRangeLimit=false;
        public double Polynomial=0;
        public boolean AmplitudeTableInterpolation=false;
        public List<Object> AmplitudeTable=new ArrayList<>();
    }
}