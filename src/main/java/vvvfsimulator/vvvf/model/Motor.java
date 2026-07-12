package vvvfsimulator.vvvf.model;
import vvvfsimulator.vvvf.MyMath;
public class Motor{
    public final MotorSpecification specification;
    public Status parameter=new Status();
    private final double[] k1=new double[5];
    private final double[] k2=new double[5];
    private final double[] k3=new double[5];
    private final double[] k4=new double[5];
    private final double[] flux=new double[4];
    private final double[] derivativeFlux=new double[4];
    private final double[] solD=new double[2];
    private final double[] solQ=new double[2];
    public Motor(MotorSpecification specification){
        this.specification=specification;
    }
    public Motor copy(){
        Motor copy=new Motor(specification.copy());
        copy.parameter=parameter.copy();
        return copy;
    }
    public void process(double dt,double we,Struct.PhaseState voltage){
        parameter.we=we;
        parameter.vdq0[0]=(Math.cos(parameter.thetaMr)*voltage.u+
                           Math.cos(parameter.thetaMr+MyMath.M_2PI/3.0)*voltage.v+
                           Math.cos(parameter.thetaMr-MyMath.M_2PI/3.0)*voltage.w)*specification.v/2.0;
        parameter.vdq0[1]=(-Math.sin(parameter.thetaMr)*voltage.u
                           -Math.sin(parameter.thetaMr+MyMath.M_2PI/3.0)*voltage.v
                           -Math.sin(parameter.thetaMr-MyMath.M_2PI/3.0)*voltage.w)*specification.v/2.0;
        double ws=parameter.we;
        double wr0=parameter.wr;
        double ids0=parameter.idq0[0];
        double iqs0=parameter.idq0[1];
        double ird0=parameter.ir[0];
        double irq0=parameter.ir[1];
        double vds=parameter.vdq0[0];
        double vqs=parameter.vdq0[1];
        derivatives(ids0,iqs0,ird0,irq0,wr0,ws,vds,vqs,k1);
        derivatives(ids0+0.5*k1[0]*dt,iqs0+0.5*k1[1]*dt,
                ird0+0.5*k1[2]*dt,irq0+0.5*k1[3]*dt,wr0+0.5*k1[4]*dt,ws,vds,vqs,k2);
        derivatives(ids0+0.5*k2[0]*dt,iqs0+0.5*k2[1]*dt,
                ird0+0.5*k2[2]*dt,irq0+0.5*k2[3]*dt,wr0+0.5*k2[4]*dt,ws,vds,vqs,k3);
        derivatives(ids0+k3[0]*dt,iqs0+k3[1]*dt,
                ird0+k3[2]*dt,irq0+k3[3]*dt,wr0+k3[4]*dt,ws,vds,vqs,k4);
        double idsNew=ids0+dt/6.0*(k1[0]+2*k2[0]+2*k3[0]+k4[0]);
        double iqsNew=iqs0+dt/6.0*(k1[1]+2*k2[1]+2*k3[1]+k4[1]);
        double irdNew=ird0+dt/6.0*(k1[2]+2*k2[2]+2*k3[2]+k4[2]);
        double irqNew=irq0+dt/6.0*(k1[3]+2*k2[3]+2*k3[3]+k4[3]);
        double wrNew=wr0+dt/6.0*(k1[4]+2*k2[4]+2*k3[4]+k4[4]);
        computeFlux(idsNew,iqsNew,irdNew,irqNew,flux);
        double psiSd=flux[0];
        double psiSq=flux[1];
        double psiRd=flux[2];
        double psiRq=flux[3];
        parameter.fluxS[0]=psiSd;
        parameter.fluxS[1]=psiSq;
        parameter.fluxR[0]=psiRd;
        parameter.fluxR[1]=psiRq;
        parameter.phiR=Math.sqrt(psiRd*psiRd+psiRq*psiRq);
        parameter.te=1.5*specification.np*(psiSd*iqsNew-psiSq*idsNew);
        parameter.wsl=ws-wrNew;
        parameter.idq0[0]=idsNew;
        parameter.idq0[1]=iqsNew;
        parameter.ir[0]=irdNew;
        parameter.ir[1]=irqNew;
        parameter.wr=wrNew;
        parameter.thetaR+=parameter.wr*dt;
        parameter.thetaR%=2.0*Math.PI;
        if(parameter.thetaR<0) parameter.thetaR+=2.0*Math.PI;
        parameter.thetaMr+=ws*dt;
        parameter.thetaMr%=2.0*Math.PI;
        if(parameter.thetaMr<0) parameter.thetaMr+=2.0*Math.PI;
        parameter.diffTe=parameter.te-parameter.preTe;
        parameter.preTe=parameter.te;
        for(int i=0;i<3;i++){
            parameter.diffIdq0[i]=parameter.idq0[i]-parameter.preIdq0[i];
            parameter.preIdq0[i]=parameter.idq0[i];
        }
    }
    public void reset(){
        parameter=new Status();
    }
    private void derivatives(double ids,double iqs,double ird,double irq,
                             double wr,double ws,double vds,double vqs,double[] out){
        computeFlux(ids,iqs,ird,irq,derivativeFlux);
        double psiSd=derivativeFlux[0];
        double psiSq=derivativeFlux[1];
        double psiRd=derivativeFlux[2];
        double psiRq=derivativeFlux[3];
        solve2x2(specification.ls,specification.lm,specification.lm,specification.lr,
                vds-specification.rs*ids+ws*psiSq,-specification.rr*ird+(ws-wr)*psiRq,solD);
        double dids=solD[0];
        double dird=solD[1];
        solve2x2(specification.ls,specification.lm,specification.lm,specification.lr,
                vqs-specification.rs*iqs-ws*psiSd,-specification.rr*irq-(ws-wr)*psiRd,solQ);
        double diqs=solQ[0];
        double dirq=solQ[1];
        double te=1.5*specification.np*(psiSd*iqs-psiSq*ids);
        double tFric=frictionTorque(wr,te-parameter.tl);
        double dwr=specification.np*(te-parameter.tl-tFric)/specification.inertia;
        out[0]=dids;
        out[1]=diqs;
        out[2]=dird;
        out[3]=dirq;
        out[4]=dwr;
    }
    private void computeFlux(double ids,double iqs,double ird,double irq,double[] out){
        out[0]=specification.ls*ids+specification.lm*ird;
        out[1]=specification.ls*iqs+specification.lm*irq;
        out[2]=specification.lm*ids+specification.lr*ird;
        out[3]=specification.lm*iqs+specification.lr*irq;
    }
    private void solve2x2(double a11,double a12,double a21,double a22,double b1,double b2,double[] out){
        double det=a11*a22-a12*a21;
        if(Math.abs(det)<1e-12) det=Math.signum(det)*1e-12+1e-12;
        out[0]=(b1*a22-b2*a12)/det;
        out[1]=(a11*b2-a21*b1)/det;
    }
    private double frictionTorque(double wr,double teMinusTl){
        double absWr=Math.abs(wr);
        double epsW=1e-6;
        double stribeckAmp=specification.fs-(specification.fs-specification.fc)
                *Math.exp(-Math.pow(absWr/specification.stribeckOmega,2.0));
        double smoothSign=Math.tanh(specification.fricSmoothK*wr);
        double tVisc=specification.fd*wr;
        double tSlide=tVisc+stribeckAmp*smoothSign;
        if(Math.abs(wr)<epsW && Math.abs(teMinusTl)<specification.fs) return teMinusTl;
        return tSlide;
    }
    public static class MotorSpecification{
        public double v=1;
        public double rs=0.435;
        public double rr=0.816;
        public double ls=0.004;
        public double lr=0.004;
        public double lm=0.0035;
        public double np=2;
        public double inertia=0.089;
        public double fd=0.0;
        public double fc=0.0;
        public double fs=0.0;
        public double stribeckOmega=1.0;
        public double fricSmoothK=100.0;
        public MotorSpecification copy(){
            MotorSpecification copy=new MotorSpecification();
            copy.v=v;
            copy.rs=rs;
            copy.rr=rr;
            copy.ls=ls;
            copy.lr=lr;
            copy.lm=lm;
            copy.np=np;
            copy.inertia=inertia;
            copy.fd=fd;
            copy.fc=fc;
            copy.fs=fs;
            copy.stribeckOmega=stribeckOmega;
            copy.fricSmoothK=fricSmoothK;
            return copy;
        }
    }
    public static class Status{
        public double[] idq0=new double[3];
        public double[] vdq0=new double[3];
        public double wsl;
        public double wr;
        public double[] ir=new double[2];
        public double we;
        public double thetaR;
        public double thetaMr;
        public double tl;
        public double te;
        public double phiR;
        public double[] fluxS=new double[2];
        public double[] fluxR=new double[2];
        public double preTe;
        public double diffTe;
        public double[] preIdq0=new double[3];
        public double[] diffIdq0=new double[3];
        public Status copy(){
            Status copy=new Status();
            copy.idq0=idq0.clone();
            copy.vdq0=vdq0.clone();
            copy.wsl=wsl;
            copy.wr=wr;
            copy.ir=ir.clone();
            copy.we=we;
            copy.thetaR=thetaR;
            copy.thetaMr=thetaMr;
            copy.tl=tl;
            copy.te=te;
            copy.phiR=phiR;
            copy.fluxS=fluxS.clone();
            copy.fluxR=fluxR.clone();
            copy.preTe=preTe;
            copy.diffTe=diffTe;
            copy.preIdq0=preIdq0.clone();
            copy.diffIdq0=diffIdq0.clone();
            return copy;
        }
    }
}