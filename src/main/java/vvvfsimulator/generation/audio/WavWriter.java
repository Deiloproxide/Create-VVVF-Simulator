package vvvfsimulator.generation.audio;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
public final class WavWriter{
    public static void writeHeader(BufferedOutputStream out,int sampleRate,
                                   int bitsPerSample,int channels,int dataSize) throws IOException{
        int byteRate=sampleRate*channels*bitsPerSample/8;
        int blockAlign=channels*bitsPerSample/8;
        int chunkSize=36+dataSize;
        writeAscii(out,"RIFF");
        writeIntLE(out,chunkSize);
        writeAscii(out,"WAVE");
        writeAscii(out,"fmt ");
        writeIntLE(out,16);
        writeShortLE(out,1);
        writeShortLE(out,channels);
        writeIntLE(out,sampleRate);
        writeIntLE(out,byteRate);
        writeShortLE(out,blockAlign);
        writeShortLE(out,bitsPerSample);
        writeAscii(out,"data");
        writeIntLE(out,dataSize);
    }
    public static void patchDataSize(String path,int dataSize) throws IOException{
        try(RandomAccessFile raf=new RandomAccessFile(new File(path),"rw")){
            raf.seek(4);
            raf.writeInt(Integer.reverseBytes(36+dataSize));
            raf.seek(40);
            raf.writeInt(Integer.reverseBytes(dataSize));
        }
    }
    public static BufferedOutputStream createWithHeader(String path,int sampleRate,
                                                        int bitsPerSample,int channels) throws IOException{
        BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(path));
        writeHeader(out,sampleRate,bitsPerSample,channels,0);
        return out;
    }
    private static void writeAscii(BufferedOutputStream out,String value) throws IOException{
        out.write(value.getBytes());
    }
    private static void writeIntLE(BufferedOutputStream out,int value) throws IOException{
        out.write(value&0xFF);
        out.write((value>>8)&0xFF);
        out.write((value>>16)&0xFF);
        out.write((value>>24)&0xFF);
    }
    private static void writeShortLE(BufferedOutputStream out,int value) throws IOException{
        out.write(value&0xFF);
        out.write((value>>8)&0xFF);
    }
}