package createvvvfsim;
import com.simibubi.create.content.trains.entity.Train;
import genengine.BaseSoundGen;
import genengine.TrackSoundGen;
import genengine.VVVFSoundGen;
import genengine.WindSoundGen;
public class TrainData{
    public final Train train;
    public final BaseSoundGen base_gen=new BaseSoundGen();
    public final VVVFSoundGen vvvf_gen=new VVVFSoundGen();
    public final WindSoundGen wind_gen=new WindSoundGen();
    public final TrackSoundGen track_gen=new TrackSoundGen();
    public final FSmoother f_smoother=new FSmoother();
    public boolean is_reloaded=false,is_move=false,is_last_move=false;
    public TrainData(Train train){
        this.train=train;
    }
}