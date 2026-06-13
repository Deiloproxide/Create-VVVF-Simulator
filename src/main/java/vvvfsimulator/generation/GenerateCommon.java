package vvvfsimulator.generation;
import vvvfsimulator.data.basefrequency.StructCompiled;
import vvvfsimulator.data.trainaudio.Struct;
public final class GenerateCommon{
    public static class GenerationParameter{
        public StructCompiled baseFrequencyData;
        public vvvfsimulator.data.vvvf.Struct vvvfData;
        public Struct trainData;
        public Progress progress=new Progress();
        public GenerationParameter(StructCompiled baseFrequencyData,
                                   vvvfsimulator.data.vvvf.Struct vvvfData,Struct trainData){
            this.baseFrequencyData=baseFrequencyData;
            this.vvvfData=vvvfData;
            this.trainData=trainData;
        }
    }
    public static class Progress{
        public volatile double total;
        public volatile double progress;
        public volatile boolean cancel;
    }
}