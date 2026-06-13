package vvvfsimulator.vvvf.modulation;
public class DeltaSigma{
    private double integrator=0.0;
    private double lastProcessTime=0.0;
    private double lastUpdateTime=0.0;
    private int lastOutBit=0;
    public double feedbackInterval=1e-4;
    public DeltaSigma copy(){
        DeltaSigma clone=new DeltaSigma();
        clone.integrator=integrator;
        clone.lastProcessTime=lastProcessTime;
        clone.lastUpdateTime=lastUpdateTime;
        clone.lastOutBit=lastOutBit;
        clone.feedbackInterval=feedbackInterval;
        return clone;
    }
    public int process(double input,double nowTime){
        double dt=nowTime-lastProcessTime;
        lastProcessTime=nowTime;
        double quantized=(lastOutBit==1)?1.0:-1.0;
        integrator+=(input-quantized)*dt;
        if(nowTime-lastUpdateTime>=feedbackInterval){
            lastOutBit=(integrator>=0.0)?1:0;
            lastUpdateTime=nowTime;
        }
        return lastOutBit;
    }
    public void reset(){
        reset(0.0);
    }
    public void reset(double nowTime){
        integrator=0.0;
        lastOutBit=0;
        lastUpdateTime=nowTime;
        lastProcessTime=nowTime;
    }
    public void resetIfLastTime(double lastTime){
        if(lastTime!=lastProcessTime) reset(lastTime);
    }
}