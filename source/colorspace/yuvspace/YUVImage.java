package colorspace.yuvspace;

public interface YUVImage {

  public int getWidth();
  public int getHeight();
  public int getOneFrameSize();
  // public int getUIndex();
  // public int getVIndex();
  public void setSize(int width, int height);
  
  public int[] convertYUVtoRGB(byte[] yuv);
  public byte[] convertRGBtoYUV(int[] rgb);

}
