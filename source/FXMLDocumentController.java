

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import colorspace.yuvspace.*;


public class FXMLDocumentController implements Initializable {

    static final class YUVFileHandle {

        private YUVImage yuvImage;
        private RandomReadYUVFile yuvInput;

        public YUVFileHandle(File file, YUVImage yuvImage) {
            this.yuvImage = yuvImage;
            setYUVInput(file, yuvImage);
        }

        public YUVImage getYUVImage() {
            return yuvImage;
        }

        public RandomReadYUVFile getYUVInput() {
            return yuvInput;
        }

        public void setCurrentFrameIndex(int index) {
            try {
                if (index >= 0 && yuvInput.getTotalFrameNumbers() > 0) {
                    yuvInput.setCurrentFrameIndex(index);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public long getCurrentFrameIndex() {
            if (yuvInput != null) {
                return yuvInput.getCurrentFrameIndex();
            }
            return -1;
        }

        public long getTotalFrameNumbers() {
            if (yuvInput != null) {
                return yuvInput.getTotalFrameNumbers();
            }
            return -1;
        }

        // change yuvImage Resolution but don't change yuvImage colorspace
        public void setResolution(int width, int height) {
            yuvImage.setSize(width, height);
            setColorSpace(yuvImage);
        }

        public void setColorSpace(YUVImage yuvImage) {
            this.yuvImage = yuvImage;
            if (yuvInput != null) {
                yuvInput.setYUVImage(yuvImage);
                setCurrentFrameIndex(0); // need reset to file start position
            }
        }

        public void setYUVInput(File file, YUVImage yuvImage) {
            try {
                closeYUVFile(); // must close the file, before open a new file.
                if (file != null) {
                    yuvInput = new RandomReadYUVFile(file, yuvImage);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void readYUV(byte[] yuvFrame) {
            try {
                if (yuvInput != null) {
                    yuvInput.read(yuvFrame);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public int[] readYUVtoARGB() {
            byte[] yuvFrame = new byte[yuvImage.getOneFrameSize()];
            readYUV(yuvFrame);
            return yuvImage.convertYUVtoRGB(yuvFrame);
        }

        public void closeYUVFile() {
            if (yuvInput != null) {
                try {
                    yuvInput.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private YUVFileHandle yuvFileHandle;

    @FXML
    private ImageView imageView;
    @FXML
    private javafx.scene.control.Slider slider;
    @FXML
    private TextField heightText;
    @FXML
    private TextField widthText;
    @FXML
    private TextField frameNumberText;
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private void handleMenuItemClose(ActionEvent event) {
        Stage stage = (Stage) imageView.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMenuItemOpen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        java.nio.file.Path currentRelativePath = java.nio.file.Paths.get(".");
        fileChooser.setInitialDirectory(currentRelativePath.toFile());
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("YUV File", "*.yuv"), new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (selectedFile != null) {
            yuvFileHandle.setYUVInput(selectedFile, yuvFileHandle.getYUVImage());
            setSliderRange((int) yuvFileHandle.getTotalFrameNumbers() - 1);
            showImage();
        }
    }

    private void showImage() {
        // Create WritableImage
        WritableImage wImage = new WritableImage(yuvFileHandle.getYUVImage().getWidth(),
                yuvFileHandle.getYUVImage().getHeight());
        wImage.getPixelWriter().setPixels(0, 0,
                yuvFileHandle.getYUVImage().getWidth(), yuvFileHandle.getYUVImage().getHeight(),
                PixelFormat.getIntArgbInstance(), yuvFileHandle.readYUVtoARGB(), 0,
                yuvFileHandle.getYUVImage().getWidth());

        imageView.setImage(wImage);

        if (!imageView.fitHeightProperty().isBound() && !imageView.fitWidthProperty().isBound()) {
            imageView.setFitHeight(yuvFileHandle.getYUVImage().getHeight());
            imageView.setFitWidth(yuvFileHandle.getYUVImage().getWidth());
        }
    }

    @FXML
    private void handleMenuItemYUVSpace(ActionEvent event) {
        //System.out.println( ((javafx.scene.control.RadioMenuItem)event.getSource()).getId() );
        switch (((javafx.scene.control.RadioMenuItem) event.getSource()).getId()) {
            case "YUV411":
                yuvFileHandle.setColorSpace(new YUV411(yuvFileHandle.yuvImage.getWidth(),
                        yuvFileHandle.yuvImage.getHeight()));
                break;
            case "YUV420":
                yuvFileHandle.setColorSpace(new YUV420(yuvFileHandle.yuvImage.getWidth(),
                        yuvFileHandle.yuvImage.getHeight()));
                break;
            case "YUV422":
                yuvFileHandle.setColorSpace(new YUV422(yuvFileHandle.yuvImage.getWidth(),
                        yuvFileHandle.yuvImage.getHeight()));
                break;
            case "YUV444":
                yuvFileHandle.setColorSpace(new YUV444(yuvFileHandle.yuvImage.getWidth(),
                        yuvFileHandle.yuvImage.getHeight()));
                break;
        }
        setSliderRange((int) yuvFileHandle.getTotalFrameNumbers() - 1);
        showImage();
    }

    @FXML
    private void handleMenuItemResolution(ActionEvent event) {
        widthText.setEditable(false);
        heightText.setEditable(false);

        switch (((javafx.scene.control.RadioMenuItem) event.getSource()).getId()) {
            case "HD1080P(1920x1080)":
                yuvFileHandle.setResolution(1920, 1080);
                widthText.setText("1920");
                heightText.setText("1080");
                break;
            case "HD720P(1280x700)":
                yuvFileHandle.setResolution(1280, 700);
                widthText.setText("1280");
                heightText.setText("700");
                break;
            case "SVGA(800x600)":
                yuvFileHandle.setResolution(800, 600);
                widthText.setText("800");
                heightText.setText("600");
                break;
            case "CIF(352x288)":
                yuvFileHandle.setResolution(352, 288);
                widthText.setText("352");
                heightText.setText("288");
                break;
            case "QCIF(176x144)":
                yuvFileHandle.setResolution(176, 144);
                widthText.setText("176");
                heightText.setText("144");
                break;
            case "Custom":
                widthText.setEditable(true);
                heightText.setEditable(true);
                break;
        }
        setSliderRange((int) yuvFileHandle.getTotalFrameNumbers() - 1);
        showImage();
    }

    @FXML
    private void handleMenuItemZoom(ActionEvent event) {
        switch (((javafx.scene.control.RadioMenuItem) event.getSource()).getId()) {
            case "FollowImageSize":
                imageView.fitHeightProperty().unbind();
                imageView.fitWidthProperty().unbind();
                imageView.setFitHeight(yuvFileHandle.getYUVImage().getHeight());
                imageView.setFitWidth(yuvFileHandle.getYUVImage().getWidth());
                break;
            case "FollowWindowSize":
                imageView.fitHeightProperty().bind(scrollPane.heightProperty().add(-2));
                imageView.fitWidthProperty().bind(scrollPane.widthProperty().add(-2));
                break;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        yuvFileHandle = new YUVFileHandle(null, new YUV420(352, 288));

        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov,
                    Number olVal, Number newVal) {

                frameNumberText.setText((newVal.intValue() + 1) + "");
                yuvFileHandle.setCurrentFrameIndex(newVal.intValue());
                showImage();
            }
        });
        slider.setBlockIncrement(1);
        setSliderRange(0);

        imageView.fitHeightProperty().bind(scrollPane.heightProperty().add(-2));
        imageView.fitWidthProperty().bind(scrollPane.widthProperty().add(-2));
        imageView.setPreserveRatio(false);

        widthText.setEditable(false);
        heightText.setEditable(false);

        setTextFieldListener(widthText,
                textField -> {
                    if (isInLimitInteger(textField.getText())) {
                        yuvFileHandle.setResolution(Integer.valueOf(textField.getText()),
                                yuvFileHandle.getYUVImage().getHeight());
                        setSliderRange((int) yuvFileHandle.getTotalFrameNumbers() - 1);
                        showImage();
                    }
                });
        setTextFieldListener(heightText,
                textField -> {
                    if (isInLimitInteger(textField.getText())) {
                        yuvFileHandle.setResolution(yuvFileHandle.getYUVImage().getWidth(),
                                Integer.valueOf(textField.getText()));
                        setSliderRange((int) yuvFileHandle.getTotalFrameNumbers() - 1);
                        showImage();
                    }
                });

        Platform.runLater(this::setStageOnHiddenEvent);
    }


    private void setStageOnHiddenEvent() {
        imageView.getScene().getWindow().setOnHidden(event -> yuvFileHandle.closeYUVFile());
    }

    private void setTextFieldListener(TextField textField, Consumer<TextField> consumer) {
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue) {
                if (newValue == false) {
                    consumer.accept(textField);
                }
            }
        });
        textField.setOnAction(event -> {
            consumer.accept(textField);
        });
    }

    private void setSliderRange(int range) {
        if (range > 0) {
            slider.setValue(0);
            slider.setMin(0);
            slider.setMax(range);
        } else {
            slider.setValue(0);
            slider.setMin(0);
            slider.setMax(0);
        }
    }

    private boolean isInteger(String str) {
        return str.matches("\\d+");
    }

    private boolean isInLimitInteger(Integer value) {
        if (value > 9999) {
            return false;
        } else if (value < 0) {
            return false;
        }
        return true;
    }

    private boolean isInLimitInteger(String str) {
        if (isInteger(str)) {
            if (isInLimitInteger(Integer.valueOf(str))) {
                return true;
            }
        }
        return false;
    }
}
