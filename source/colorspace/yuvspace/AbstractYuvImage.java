package colorspace.yuvspace;

public abstract class AbstractYuvImage implements YUVImage{
  protected int width;
  protected int height;
  protected int oneFrameSize;
  protected int uIndex;
  protected int vIndex;
  
  public AbstractYuvImage(int width, int height){
    this.setSize(width, height);
  }
  
  protected void setWidth(int width){
    this.width = width;
  }  
  protected void setHeight(int height){
    this.height = height;
  }
  protected void setUIndex(int uIndex){
    this.uIndex = uIndex;
  }
  protected void setVIndex(int vIndex){
    this.vIndex = vIndex;
  }
  protected void setOneFrameSize(int oneFrameSize){
    this.oneFrameSize = oneFrameSize;
  }

  
  @Override
  public void setSize(int width, int height) {
    this.setWidth(width);
    this.setHeight(height);
    this.setUpYuvImage(width, height);
  }
  @Override
  public int getWidth() {
    return width;
  }
  @Override
  public int getHeight() {
    return height;
  }
  @Override
  public int getOneFrameSize() {
    return oneFrameSize;
  }
  
  protected abstract void setUpYuvImage(int width, int height);
  @Override
  public abstract int[] convertYUVtoRGB(byte[] yuv);
  @Override
  public abstract byte[] convertRGBtoYUV(int[] rgb);
}