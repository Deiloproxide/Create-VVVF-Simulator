package createvvvfsim;
import com.simibubi.create.content.trains.entity.Train;
public class TrainData{
    public final Train train;
    public final SoundGen gen=new SoundGen();
    public final VVVFSoundGen vvvf_gen=new VVVFSoundGen();
    public final FSmoother f_smoother=new FSmoother();
    public boolean is_reloaded=false;
    public boolean is_move=false,is_last_move=false;
    public TrainData(Train train){
        this.train=train;
    }
}