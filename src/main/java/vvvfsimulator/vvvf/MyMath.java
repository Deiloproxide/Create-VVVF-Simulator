package vvvfsimulator.vvvf;
public class MyMath{
    public static final double M_2_PI=0.63661977236758134307553505349006;
    public static final double M_1_PI=0.31830988618379067153776752674503;
    public static final double M_1_2PI=0.15915494309189533576888376337251;
    public static final double M_4PI_3=4.1887902047863909846168578443727;
    public static final double M_2PI=6.283185307179586476925286766559;
    public static final double M_2PI_3=2.0943951023931954923084289221863;
    public static final double M_PI=3.1415926535897932384626433832795;
    public static final double M_PI_2=1.5707963267948966192313216916398;
    public static final double M_PI_3=1.0471975511965977461542144610932;
    public static final double M_PI_4=0.78539816339744830961566084581988;
    public static final double M_PI_6=0.52359877559829887307710723054658;
    public static final double M_PI_12=0.26179938779914943653855361527329;
    public static final double M_PI_180=0.01745329251994329576923690768489;
    public static final double M_SQRT3=1.7320508075688772935274463415059;
    public static final double M_SQRT3_2=0.86602540378443864676372317075294;
    public static class Functions{
        public static double triangle(double x){
            double phase=M_2_PI*x-4.0*Math.floor(x*M_1_2PI);
            if(1.0<=phase && phase<3.0) return 2.0-phase;
            if(3.0<=phase) return phase-4.0;
            return phase;
        }
        public static double saw(double x){
            return M_1_PI*x-2.0*Math.floor(x*M_1_2PI)-1.0;
        }
        public static double sine(double x){
            return Math.sin(x);
        }
        public static double arcSine(double x){
            return Math.asin(x);
        }
        public static double square(double x){
            double fixedX=x-Math.floor(x*M_1_2PI)*M_2PI;
            return fixedX*M_1_PI>1.0?-1.0:1.0;
        }
    }
}