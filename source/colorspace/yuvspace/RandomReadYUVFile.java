package colorspace.yuvspace;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.Closeable;
import java.io.FileNotFoundException;

public class RandomReadYUVFile implements Closeable {
  private long indexOfTheFrame = 0;  
  private YUVImage yuvImage;
  private File yuvFile;
  private RandomAccessFile input;
  
  public RandomReadYUVFile(File file, YUVImage yuvImage) throws FileNotFoundException{
    yuvFile = file;
    this.yuvImage = yuvImage;
    input = new RandomAccessFile(file, "r");
  }
  
  public void setYUVImage(YUVImage yuvImage) {
    this.yuvImage = yuvImage;
  }
  
  public int getTotalFrameNumbers() {
    int oneFrameSize = yuvImage.getOneFrameSize();
    int totalFrameNumber = (int) (yuvFile.length() / oneFrameSize); 
    return totalFrameNumber;
  }

  public void setCurrentFrameIndex(long indexOfTheFrame) throws IOException {
    if(indexOfTheFrame < this.getTotalFrameNumbers()) {
      this.indexOfTheFrame = indexOfTheFrame;
      input.seek(indexOfTheFrame * yuvImage.getOneFrameSize());
    } else {
      throw new IOException("Total Frames are " + this.getTotalFrameNumbers() + 
        ", indexOfTheFrame = " + indexOfTheFrame);
    }
  }
  
  public long getCurrentFrameIndex() {
    return indexOfTheFrame;
  }
  
  public int read(byte[] yuvFrame)  throws IOException {
    int oneFrameSize = yuvImage.getOneFrameSize();
    return input.read(yuvFrame, 0, oneFrameSize);
  }
  
  boolean closed = false;
  @Override
  public void close() throws IOException {
    if(closed == false) {
      input.close();
      closed = true;
    }
  }
}