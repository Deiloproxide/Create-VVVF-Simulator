package vvvfsimulator.generation.audio.vvvfsound;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import vvvfsimulator.data.basefrequency.Analyze;
import vvvfsimulator.generation.GenerateCommon.GenerationParameter;
import vvvfsimulator.generation.audio.WavWriter;
import vvvfsimulator.vvvf.MyMath;
import vvvfsimulator.vvvf.calculation.Common;
import vvvfsimulator.vvvf.model.Struct;
import vvvfsimulator.vvvf.model.Struct.Domain;
public final class Audio{
    public static void exportWavLine(GenerationParameter param,
                                     int samplingFreq,String path) throws IOException{
        exportWavFile(param,samplingFreq,path,Mode.Line);
    }
    public static void exportWavPhaseCurrent(GenerationParameter param,
                                             int samplingFreq,String path) throws IOException{
        exportWavFile(param,samplingFreq,path,Mode.PhaseCurrent);
    }
    private static void exportWavFile(GenerationParameter parameter,
                                      int samplingFreq,String path,Mode mode) throws IOException{
        double dt=1.0/samplingFreq;
        parameter.progress.total=parameter.baseFrequencyData.getEstimatedSteps(dt);
        Domain domain=new Domain(parameter.trainData.motorSpec);
        try(BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(path))){
            WavWriter.writeHeader(out,samplingFreq,16,1,0);
            int sampleCount=0;
            while(true){
                vvvfsimulator.data.vvvf.Analyze.calculate(domain,parameter.vvvfData);
                Struct.PhaseState value=Common.calculatePhaseState(domain,mode==Mode.Line?MyMath.M_PI_6:0);
                double sample=switch(mode){
                    case Line->(value.u-value.v)/2.0;
                    case PhaseCurrent->(2.0*value.u-value.v-value.w)/4.0;
                };
                short pcm=(short)Math.clamp(sample*0.35*Short.MAX_VALUE,Short.MIN_VALUE,Short.MAX_VALUE);
                out.write(pcm&0xFF);
                out.write((pcm>>8)&0xFF);
                sampleCount++;
                parameter.progress.progress++;
                boolean cont=Analyze.checkForFreqChange(domain,parameter.baseFrequencyData,parameter.vvvfData,dt);
                if(!cont || parameter.progress.cancel){
                    WavWriter.patchDataSize(path,sampleCount*2);
                    break;
                }
            }
        }
    }
    private enum Mode{
        Line,PhaseCurrent
    }
}