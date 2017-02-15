package colorspace.yuvspace;

public class YUV444 extends AbstractYuvImage {

  public YUV444(int width, int height) {
    super(width, height); 
  }
  
  @Override
  protected void setUpYuvImage(int width, int height){
    int uIndex;
    int vIndex;
    int oneFrameSize;
    int uvRowBytes = width;
    int uvSize = uvRowBytes * height;
    oneFrameSize = width * height + uvSize * 2;
    uIndex = width * height;
    vIndex = uIndex + uvSize;
    
    this.setUIndex(uIndex);
    this.setVIndex(vIndex);
    this.setOneFrameSize(oneFrameSize);
  }
  
  @Override
  public int[] convertYUVtoRGB(byte[] yuv) {

    int y, u, v;
    int a, r, g, b;
    a = 0xFF000000;
    int[] rgb = new int[width*height];
    for(int i = 0; i < width * height; i++) { 
      y = yuv[i] & 0xFF;
      u = yuv[uIndex + i] & 0xFF;
      v = yuv[vIndex + i] & 0xFF;   
      
      r = (int)(y + 1.3075 * (v - 128));
      r = (r < 0 ? 0 : (r > 255 ? 255 : r));
      
      g = (int)(y - 0.3455 * (u - 128) - 0.7169  * (v - 128));
      g = (g < 0 ? 0 : (g > 255 ? 255 : g));
      
      b = (int)(y + 1.7790 * (u - 128));  
      b = (b < 0 ? 0 : (b > 255 ? 255 : b));
      
      rgb[i] = a | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
    }
    return rgb;
  }
  
  @Override
  public byte[] convertRGBtoYUV(int[] rgb) {
    int a, r, g, b;
    byte[] yuvFrame = new byte[oneFrameSize];
    for(int i = 0; i < width * height; i++) {        
      a = (rgb[i] & 0xFF000000) >> 24;
      r = (rgb[i] & 0xFF0000) >> 16;
      g = (rgb[i] & 0xFF00) >> 8;
      b = (rgb[i] & 0xFF) >> 0;
      // Y
      yuvFrame[i]  = (byte)(0.299 * r + 0.587 * g + 0.114 * b);
      // U
      yuvFrame[uIndex + i] = (byte)(-0.169 * r - 0.331 * g + 0.5 * b + 128);
      // V
      yuvFrame[vIndex + i] = (byte)(0.5 * r - 0.419 * g - 0.081 * b + 128);
    }
    return yuvFrame;
  }
}