package colorspace.yuvspace;

public class YUV411 extends AbstractYuvImage {

  public YUV411(int width, int height) {
    super(width, height);
  }
  
  @Override
  protected void setUpYuvImage(int width, int height){
    int uIndex;
    int vIndex;
    int oneFrameSize;
    int uvRowBytes; // how many bytes uv in a row
    int uvSize;
    if(width % 4 != 0) {
      uvRowBytes = (width + 4) / 4;
    } else {
      uvRowBytes = width / 4;
    }
    
    uvSize = uvRowBytes * height;
    oneFrameSize = width * height + uvSize * 2;
    uIndex = width * height;
    vIndex = uIndex + uvSize;
    
    this.setUIndex(uIndex);
    this.setVIndex(vIndex);
    this.setOneFrameSize(oneFrameSize);
  }
  
  @Override
  public int[] convertYUVtoRGB(byte[] yuv) {
    
    int[] rgb = new int[width*height];
    int yValue = 0;
    int uValue = 0;
    int vValue = 0;
    int uAndvPosition = 0;
    int a = 0xFF000000;
    int r, g, b;
    for(int i = 0; i < width*height; i++) {
      yValue = yuv[i] & 0xFF;
      if(((i%width)%4) == 0) {
        // U 
        uValue = yuv[uIndex + uAndvPosition] & 0xFF;
        // V
        vValue = yuv[vIndex + uAndvPosition] & 0xFF;
        uAndvPosition++;
      }   
      
      r = (int)(yValue + 1.4075 * (vValue - 128));
      r = (r < 0 ? 0 : (r > 255 ? 255 : r));
      g = (int)(yValue - 0.3455 * (uValue - 128) - 0.7169  * (vValue - 128));
      g = (g < 0 ? 0 : (g > 255 ? 255 : g));
      b = (int)(yValue + 1.7790 * (uValue - 128));  
      b = (b < 0 ? 0 : (b > 255 ? 255 : b));
      
      rgb[i] = a | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);  
    }
    return rgb;
  }
  
  @Override 
  public byte[] convertRGBtoYUV(int[] rgb) {
    byte[] yuvFrame = new byte[oneFrameSize];

    int a, r, g, b;
    int uAndvPosition = 0;
    for(int i = 0; i < width*height; i++) {
        a = (rgb[i] & 0xFF000000) >> 24;
        r = (rgb[i] & 0xFF0000) >> 16;
        g = (rgb[i] & 0xFF00) >> 8;
        b = (rgb[i] & 0xFF) >> 0;
        // Y
        yuvFrame[i]  = (byte)(0.299 * r + 0.587 * g + 0.114 * b);
        if(((i%width)%4) == 0) {
          // U 
          yuvFrame[uIndex + uAndvPosition] = (byte)(-0.169 * r - 0.331 * g + 0.5 * b + 128);
          // V
          yuvFrame[vIndex + uAndvPosition] = (byte)(0.5 * r - 0.419 * g - 0.081 * b + 128);
          
          uAndvPosition++;
        }  
    }
    return yuvFrame;
  }
}