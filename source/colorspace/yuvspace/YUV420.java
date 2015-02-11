package colorspace.yuvspace;

public class YUV420 implements YUVImage {
  protected int width;
  protected int height;
  protected int oneFrameSize;
  protected int uIndex;
  protected int vIndex;

  public YUV420(int width, int height) {
    setUpYUV420Image(width, height);    
  }
  /* When change the image size, not only change the size (width and height)
   * but also change oneFrameSize, uIndex, and vIndex.
   * So use the setUpYUV420Image() to set them.
  */
  private void setUpYUV420Image(int width, int height) {
    this.width = width;
    this.height = height;
    int uvRowBytes;
    int halfHeight;
    int uvSize;
    if(width % 2 != 0) {
      uvRowBytes = (width + 1) / 2;
    } else {
      uvRowBytes = width / 2;
    }
    if(height % 2 != 0){
      halfHeight = (height + 1) / 2;
    } else {
      halfHeight = height / 2;
    }
    uvSize = uvRowBytes * halfHeight;
    oneFrameSize = width * height + uvSize * 2;
    uIndex = width * height;
    vIndex = uIndex + uvSize;
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
  public int getUIndex() { 
    return uIndex;
  }
  public int getVIndex() {
    return vIndex;
  }
  @Override
  public void setSize(int width, int height) {
    setUpYUV420Image(width, height);
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

      if((i%2) == 0 && (i/width)%2 == 0) {
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
      /*
      /* if the height is odd, then the last line must have only one line
      /* for example: a 7x5 image
      /* |00|01|02|03|04|05|
      /* |06|07|08|09|10|11|
      /------------------------
      /* |12|13|14|15|16|17|
      /* |18|19|20|21|22|23|
      /------------------------
      /* |24|25|26|27|28|29|     // only one line
      */
      if(i+width > rgb.length-1) { // so skip
        continue;
      }
      //------------------------------------------------------------------------
      yValue = yuv[i+width] & 0xFF;
      r = (int)(yValue + 1.4075 * (vValue - 128));
      r = (r < 0 ? 0 : (r > 255 ? 255 : r));
      g = (int)(yValue - 0.3455 * (uValue - 128) - 0.7169  * (vValue - 128));
      g = (g < 0 ? 0 : (g > 255 ? 255 : g));
      b = (int)(yValue + 1.7790 * (uValue - 128));  
      b = (b < 0 ? 0 : (b > 255 ? 255 : b));
      rgb[i+width] = a | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
      //------------------------------------------------------------------------
      if( ((i+1)/width)%2 != 0 &&  (i+1)%width == 0 ) {
        i = i + width;
      }
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
        b = (rgb[i] & 0xFF);
        // Y
        yuvFrame[i]  = (byte)(0.299 * r + 0.587 * g + 0.114 * b);
        if(i%2 == 0 && (i/width)%2 == 0) {
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