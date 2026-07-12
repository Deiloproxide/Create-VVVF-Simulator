package yamlloader;
public class LoadContext{
    public final LoadException exception;
    public final int row;
    public final int col;
    public LoadContext(LoadException exception,int row,int col){
        this.exception=exception;
        this.row=row;
        this.col=col;
    }
}