package vvvfsimulator.vvvf.modulation;
import vvvfsimulator.vvvf.MyMath;
public final class SVM{
    public static class FunctionTime{
        public double t0;
        public double t1;
        public double t2;
        public FunctionTime mul(double d){
            FunctionTime result=new FunctionTime();
            result.t0=t0*d;
            result.t1=t1*d;
            result.t2=t2*d;
            return result;
        }
        public Vabc getVabc(int sector){
            Vabc res=new Vabc();
            switch(sector){
                case 1->{
                    res.u=t1+t2+0.5*t0;
                    res.v=t2+0.5*t0;
                    res.w=0.5*t0;
                }
                case 2->{
                    res.u=t1+0.5*t0;
                    res.v=t1+t2+0.5*t0;
                    res.w=0.5*t0;
                }
                case 3->{
                    res.u=0.5*t0;
                    res.v=t1+t2+0.5*t0;
                    res.w=t2+0.5*t0;
                }
                case 4->{
                    res.u=0.5*t0;
                    res.v=t1+0.5*t0;
                    res.w=t1+t2+0.5*t0;
                }
                case 5->{
                    res.u=t2+0.5*t0;
                    res.v=0.5*t0;
                    res.w=t1+t2+0.5*t0;
                }
                case 6->{
                    res.u=t1+t2+0.5*t0;
                    res.v=0.5*t0;
                    res.w=t1+0.5*t0;
                }
            }
            return res;
        }
    }
    public static class Vabc{
        public double u;
        public double v;
        public double w;
        public Valbe clark(){
            Valbe result=new Valbe();
            result.alpha=(2.0*u-v-w)/3.0;
            result.beta=(v-w)/MyMath.M_SQRT3;
            return result;
        }
    }
    public static class Valbe{
        public double alpha;
        public double beta;
        public FunctionTime getFunctionTime(int sector){
            FunctionTime ft=new FunctionTime();
            switch(sector){
                case 1->{
                    ft.t1=MyMath.M_SQRT3_2*alpha-0.5*beta;
                    ft.t2=beta;
                }
                case 2->{
                    ft.t1=MyMath.M_SQRT3_2*alpha+0.5*beta;
                    ft.t2=0.5*beta-MyMath.M_SQRT3_2*alpha;
                }
                case 3->{
                    ft.t1=beta;
                    ft.t2=-(MyMath.M_SQRT3_2*alpha+0.5*beta);
                }
                case 4->{
                    ft.t1=0.5*beta-MyMath.M_SQRT3_2*alpha;
                    ft.t2=-beta;
                }
                case 5->{
                    ft.t1=-(MyMath.M_SQRT3_2*alpha+0.5*beta);
                    ft.t2=MyMath.M_SQRT3_2*alpha-0.5*beta;
                }
                case 6->{
                    ft.t1=-beta;
                    ft.t2=MyMath.M_SQRT3_2*alpha+0.5*beta;
                }
            }
            ft.t0=1.0-ft.t1-ft.t2;
            return ft;
        }
        public int estimateSector(){
            int a=beta>0.0?0:1;
            int b=beta-MyMath.M_SQRT3*alpha>0.0?0:1;
            int c=beta+MyMath.M_SQRT3*alpha>0.0?0:1;
            return switch(4*a+2*b+c){
                case 1->3;
                case 2->1;
                case 3,4->0;
                case 5->4;
                case 6->6;
                case 7->5;
                default->2;
            };
        }
    }
}