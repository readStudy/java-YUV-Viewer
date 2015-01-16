import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.text.*;
import javax.swing.filechooser.FileNameExtensionFilter;


import colorspace.yuvspace.*;

class DisplayYUVComponent extends JComponent {

  private YUVImage yuvImage;
  
  public DisplayYUVComponent(YUVImage yuvImage) {
    setYUVSpace(yuvImage);
  }
  
  private BufferedImage image;
  private byte[] yuvFrame;
  private int[] argb;
  public void setYUVSpace(YUVImage yuvImage) {
    this.yuvImage = yuvImage;
    image = new BufferedImage(yuvImage.getWidth(), yuvImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    repaint();
  }
  public void setCurrentDisplayImage(byte[] yuvFrame) {
    argb = yuvImage.convertYUVtoRGB(yuvFrame);
    repaint();
  }
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(getWidth(), getHeight()); 
  }
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if(image != null && argb != null) {
      image.setRGB(0, 0, yuvImage.getWidth(), yuvImage.getHeight(), argb, 0, yuvImage.getWidth());
      g.drawImage(image, 0, 0, getWidth(), getHeight(), null); // follow Component size
    }
  }
}

public class YUVViewer extends JFrame { 

  private JSlider frameSlider;
  private int width;
  private int height;
  private YUVImage yuvImage;
  private DisplayYUVComponent displayYUVComponent;
  private RandomReadYUVFile yuvInput = null;
  private File selectedFile;
  private byte[] yuvFrame;
  private boolean isfollowImageSize = false;
  
  private JFormattedTextField widthField; 
  private JFormattedTextField heightField;
  private JFormattedTextField frameNumberField;
  //Labels to identify the fields
  private JLabel widthLabel;
  private JLabel heightLabel;
  private JLabel frameNumberLabel;
  //Strings for the labels
  private static String widthLabelString = "Width:";
  private static String heightLabelString = "Height:";
  private static String frameNumberLabelString = "FrameNumber:";
  //Panel for the labels and the text fields.
  private JPanel labelAndTextFieldPane;

  private final static Dimension SCREEN_SIZE = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
  
  public YUVViewer() {
    
    initDisplayYUVComponent(); 
    JScrollPane scrollPane = new JScrollPane(displayYUVComponent);
    add(BorderLayout.CENTER, scrollPane);
    
    initFrameSlider();
    createFormatFields();    
    setUpLabelAndTextfieldPairs();
    // Panel for the labelAndTextFieldPane and the frameSlider.
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.WEST, labelAndTextFieldPane);
    panel.add(BorderLayout.SOUTH, frameSlider);
    add(BorderLayout.SOUTH, panel);
    
    setJMenuBar(createJMenueBar()); 
    
    addComponentListener(new ComponentAdapter() {  
      public void componentResized(ComponentEvent evt) {
          if(isfollowImageSize) {
            displayYUVComponent.setSize(yuvImage.getWidth(), yuvImage.getHeight());
            setSuitableWindowSize(yuvImage.getWidth(), yuvImage.getHeight());
          } else {
            Rectangle r = displayYUVComponent.getVisibleRect();
            displayYUVComponent.setSize((int)r.getWidth(), (int)r.getHeight());
          }
      }
    });
    
    addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e) {
        closeYUVFile();
      }
    });
    addWindowListener(new WindowAdapter(){
      public void windowClosed(WindowEvent e) {
        closeYUVFile();
      }
    });
  }
  
  private void initDisplayYUVComponent() {
    this.width = 352;
    height = 288;
    yuvImage = new YUV420(width, height);
    displayYUVComponent = new DisplayYUVComponent(yuvImage);
    displayYUVComponent.setSize(yuvImage.getWidth(), yuvImage.getHeight());
  }
  
  private void createFormatFields() {
    NumberFormat widthAndHehightFormat = NumberFormat.getIntegerInstance();
    widthAndHehightFormat.setMaximumIntegerDigits(4);
    widthAndHehightFormat.setMinimumIntegerDigits(1);
    
    widthField = new JFormattedTextField(widthAndHehightFormat);
    widthField.setColumns(4);
    widthField.setValue(yuvImage.getWidth());
    widthField.setEditable(false);
    
    heightField = new JFormattedTextField(widthAndHehightFormat);
    heightField.setColumns(4);
    heightField.setValue(yuvImage.getHeight());
    heightField.setEditable(false);
    
    frameNumberField = new JFormattedTextField();
    frameNumberField.setValue(0);
    frameNumberField.setEditable(false);
  }
  private void createLabels() {
    widthLabel = new JLabel(widthLabelString);
    heightLabel = new JLabel(heightLabelString);
    frameNumberLabel = new JLabel(frameNumberLabelString);
  }
  
  private void setUpLabelAndTextfieldPairs() {
    createLabels();
    createFormatFields();
    
    widthField.addPropertyChangeListener("value", widthAndHehightChangeListener());
    heightField.addPropertyChangeListener("value", widthAndHehightChangeListener());
    
    widthLabel.setLabelFor(widthField);
    heightLabel.setLabelFor(heightField);
    frameNumberLabel.setLabelFor(frameNumberField);
    //Layout the labels and the text fields in a panel.
    JPanel widthLabelAndWidthTextFieldPane = new JPanel(new GridLayout(0,2));
    widthLabelAndWidthTextFieldPane.add(widthLabel);
    widthLabelAndWidthTextFieldPane.add(widthField);
    
    JPanel heightLabelAndheightTextFieldPane = new JPanel(new GridLayout(0,2));
    heightLabelAndheightTextFieldPane.add(heightLabel);
    heightLabelAndheightTextFieldPane.add(heightField);
    
    JPanel frameNumberLabelAndframeNumberFieldPane = new JPanel(new GridLayout(0,2));
    frameNumberLabelAndframeNumberFieldPane.add(frameNumberLabel);
    frameNumberLabelAndframeNumberFieldPane.add(frameNumberField);
    
    labelAndTextFieldPane = new JPanel(new GridLayout(3, 0));
    labelAndTextFieldPane.add(widthLabelAndWidthTextFieldPane);
    labelAndTextFieldPane.add(heightLabelAndheightTextFieldPane);
    labelAndTextFieldPane.add(frameNumberLabelAndframeNumberFieldPane);
  }
  
  private PropertyChangeListener widthAndHehightChangeListener() {
    return new PropertyChangeListener() {
      
      public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source == widthField) {
          width = ((Number)widthField.getValue()).intValue();
          if(width > 9999) {
            width = 9999;
            widthField.setValue(9999);
            System.out.println("widthValue > 9999, widthField value = " + width);
          } else if(width < 0) {
            width = 1;
            widthField.setValue(1);
          }
        } else  {
          height = ((Number)heightField.getValue()).intValue();
        }
        
        changeResolution(width, height);
        setSuitableWindowSize(width, height);
      }
    };
  }
  
  private void initFrameSlider() {
    frameSlider = new JSlider(0, 0);
    
    frameSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
          if(yuvFrame != null) {
            setCurrentFrameIndex( ((JSlider)e.getSource()).getValue() );
            System.out.println("current index = "+ ((JSlider)e.getSource()).getValue());
            readYUV(yuvFrame);
            displayYUVComponent.setCurrentDisplayImage(yuvFrame);   
          }
      }
    }); 
  }
  private void setSliderRange(RandomReadYUVFile input) {
    if(input!=null && input.getTotalFrameNumbers()>0 ) {
      frameSlider.setMaximum(input.getTotalFrameNumbers() - 1); 
      frameSlider.setMinimum(0);
      frameSlider.setValue(0);
    }
  }
  private void setCurrentFrameIndex(int index) {
    try {
      if(yuvInput != null) {
        yuvInput.setCurrentFrameIndex(index);
        // frameNumberField.setText((index + 1) + " / " + yuvInput.getTotalFrameNumbers());
        frameNumberField.setText((index + 1) + "");
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  // change yuvImage Resolution but don't change yuv space
  private void changeResolution(int width, int height) {  
    yuvImage.setSize(width, height);
    setColorSpace(yuvImage);
  }
  // change yuv space
  private void setColorSpace(YUVImage yuvImage) {  
    this.width = yuvImage.getWidth();
    this.height = yuvImage.getHeight();
    this.yuvImage = yuvImage;
    displayYUVComponent.setYUVSpace(yuvImage);
    displayYUVComponent.revalidate(); 
    setYUVInput(selectedFile, yuvImage); 
  }
  private void setYUVInput(File file, YUVImage newYUVImage) {
    try {
      closeYUVFile(); // must close the file, before open a new file.
      if(file != null) {
        yuvInput = new RandomReadYUVFile(file, newYUVImage);
        yuvFrame = new byte[newYUVImage.getOneFrameSize()];
        //-------
        readYUV(yuvFrame);
        displayYUVComponent.setCurrentDisplayImage(yuvFrame); 
        setSliderRange(yuvInput);
        //-------
      }
    } catch(FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  private void readYUV(byte[] yuvFrame) {
    try {
      if(yuvInput != null) {
        yuvInput.read(yuvFrame);
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
  private void closeYUVFile() {  
    if(yuvInput != null) {
      try {
        yuvInput.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  private JMenuBar createJMenueBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File"),
          resolutionMenu = new JMenu("Resolution"),
          colorspaceMenu = new JMenu("Color Space"), 
          zoomMenue = new JMenu("Zoom");
    
    // fileMenu -----------------------------------------------------------
    JMenuItem openFileItem = new JMenuItem("Open");
    openFileItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        java.nio.file.Path currentRelativePath = java.nio.file.Paths.get("");
        String currentFilePath = currentRelativePath.toAbsolutePath().toString();
        JFileChooser c = new JFileChooser(currentFilePath);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("YUV file", "yuv");
        c.setFileFilter(filter);
        
        int rVal = c.showOpenDialog(YUVViewer.this); 
        if(rVal == JFileChooser.APPROVE_OPTION) { 
          closeYUVFile();
          selectedFile = c.getSelectedFile();
          setYUVInput(selectedFile, yuvImage);
        }
      }
    });
    fileMenu.add(openFileItem);
    
    JMenuItem closeFileItem = new JMenuItem("Close");
    closeFileItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    fileMenu.add(closeFileItem);
    
    menuBar.add(fileMenu);
    // End fileMenu -------------------------------------------------------   

    // resolutionMenu -----------------------------------------------------
    ButtonGroup resolutionGroup = new ButtonGroup();
    JRadioButtonMenuItem hd1080pItem = new JRadioButtonMenuItem("HD1080P(1920x1080)");
    hd1080pItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useDefaultValueChangeResolution(1920, 1080);
      }
    });
    resolutionGroup.add(hd1080pItem);
    resolutionMenu.add(hd1080pItem);
    
    
    JRadioButtonMenuItem hd720pItem = new JRadioButtonMenuItem("HD720P(1280x700)");
    hd720pItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useDefaultValueChangeResolution(1280, 700);
      }
    });
    resolutionGroup.add(hd720pItem);
    resolutionMenu.add(hd720pItem);
    
    JRadioButtonMenuItem svgaItem = new JRadioButtonMenuItem("SVGA(800x600)");
    svgaItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useDefaultValueChangeResolution(800, 600);
      }
    });
    resolutionGroup.add(svgaItem);
    resolutionMenu.add(svgaItem); 

    JRadioButtonMenuItem cifItem = new JRadioButtonMenuItem("CIF(352x288)", true);
    cifItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useDefaultValueChangeResolution(352, 288);
      }
    });
    resolutionGroup.add(cifItem);
    resolutionMenu.add(cifItem); 
    
    JRadioButtonMenuItem qcifItem = new JRadioButtonMenuItem("QCIF(176x144)");
    qcifItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        useDefaultValueChangeResolution(176, 144);
      }
    });
    resolutionGroup.add(qcifItem);
    resolutionMenu.add(qcifItem); 
    
    JRadioButtonMenuItem customResolutionItem = new JRadioButtonMenuItem("Custom");
    customResolutionItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        widthField.setEditable(true);
        heightField.setEditable(true);
      }
    });
    resolutionGroup.add(customResolutionItem);
    resolutionMenu.add(customResolutionItem); 
    
    menuBar.add(resolutionMenu);
    // End resolutionMenu -------------------------------------------------
    
    // YUVSpaceMenu -------------------------------------------------------
    ButtonGroup yuvSpaceGroup = new ButtonGroup();
    JRadioButtonMenuItem YUV444Item = new JRadioButtonMenuItem("YUV444");
    YUV444Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        yuvImage = new YUV444(width, height);
        setColorSpace(yuvImage);
        // setSuitableWindowSize(getWidth(), getHeight());
      }
    });
    yuvSpaceGroup.add(YUV444Item);
    colorspaceMenu.add(YUV444Item);
    
    JRadioButtonMenuItem YUV422Item = new JRadioButtonMenuItem("YUV422");
    YUV422Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        yuvImage = new YUV422(width, height);
        setColorSpace(yuvImage);
        // setSuitableWindowSize(getWidth(), getHeight());
      }
    });
    yuvSpaceGroup.add(YUV422Item);
    colorspaceMenu.add(YUV422Item);
    
    JRadioButtonMenuItem YUV420Item = new JRadioButtonMenuItem("YUV420", true);
    YUV420Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        yuvImage = new YUV420(width, height);
        setColorSpace(yuvImage);
      }
    });
    yuvSpaceGroup.add(YUV420Item);
    colorspaceMenu.add(YUV420Item);
    
    JRadioButtonMenuItem YUV411Item = new JRadioButtonMenuItem("YUV411");
    YUV411Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        yuvImage = new YUV411(width, height);
        setColorSpace(yuvImage);
      }
    });
    yuvSpaceGroup.add(YUV411Item);
    colorspaceMenu.add(YUV411Item);
    
    menuBar.add(colorspaceMenu);
    // End YUVSpaceMenu ---------------------------------------------------
    
    // zoomMenue-----------------------------------------------------------
    ButtonGroup zoomGroup = new ButtonGroup();
    JRadioButtonMenuItem followImageSizeItem = new JRadioButtonMenuItem("Follow Image Size");
    followImageSizeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isfollowImageSize = true;
        setSuitableWindowSize(yuvImage.getWidth(), yuvImage.getHeight());
      }
    });
    zoomGroup.add(followImageSizeItem);
    zoomMenue.add(followImageSizeItem);
    
    JRadioButtonMenuItem followWindowSizeItem = new JRadioButtonMenuItem("Follow Window Size", true);
    followWindowSizeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isfollowImageSize = false;
        setSuitableWindowSize(getWidth(), getHeight());
      }
    });
    zoomGroup.add(followWindowSizeItem);
    zoomMenue.add(followWindowSizeItem);
    
    menuBar.add(zoomMenue);
    // End zoomMenue-------------------------------------------------------
      
    return menuBar;
  } 
  private void setSuitableWindowSize(int width, int height) {
    displayYUVComponent.revalidate();
    displayYUVComponent.setSize(yuvImage.getWidth(), yuvImage.getHeight());
    if(isfollowImageSize) {
      if(width < getWidth() || height < getHeight()) { 
        if(height + 100 + 70 > SCREEN_SIZE.getHeight() - 50) {
          setSize(width, height - 85);
        } else {
          pack();
        }
      }
    } else {
      Rectangle r = displayYUVComponent.getVisibleRect();
      displayYUVComponent.setSize((int)r.getWidth(), (int)r.getHeight());
    }
  }
  private void useDefaultValueChangeResolution(int width, int height) {
    widthField.setEditable(false);
    heightField.setEditable(false);
    widthField.setValue(width);
    heightField.setValue(height);
  }
  
  public static void main(String[] args) {
  
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JFrame f = new YUVViewer();
        f.setTitle("YUVViewer");
        f.pack();
        // f.setSize(500, 500);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
       }
    });  
  }
  
}