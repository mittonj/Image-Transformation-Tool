import javax.imageio.ImageIO;
import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class imagePanel extends JPanel{

    private BufferedImage image = null;
    private Color[][] imageArray = null;
    private BufferedImage imageFromArray = null;

    // private int[][] ditherMatrix = {{0,8,2,10},
    //                                 {12,4,14,6},
    //                                 {3,11,1,9},
    //                                 {15,7,13,5}};

    // private int[][] ditherMatrix = {{0,2},
    //                                 {3,1}};

    private int[][] ditherMatrix = {{ 0, 32, 8, 40, 2, 34, 10, 42}, 
                                    {48, 16, 56, 24, 50, 18, 58, 26}, 
                                    {12, 44, 4, 36, 14, 46, 6, 38}, 
                                    {60, 28, 52, 20, 62, 30, 54, 22},
                                    { 3, 35, 11, 43, 1, 33, 9, 41},
                                    {51, 19, 59, 27, 49, 17, 57, 25},
                                    {15, 47, 7, 39, 13, 45, 5, 37},
                                    {63, 31, 55, 23, 61, 29, 53, 21} };


    protected void paintComponent(Graphics grf){
        super.paintComponent(grf);
        Graphics2D g = (Graphics2D)grf;

        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if(image != null) {
            g.drawImage(imageFromArray,0,0,null);
        }
    }
 
    public boolean isNull() {
        return image == null;
    }
    
    public void displayImage() {

        if(image != null) {
            this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            removeAll();
            revalidate();
            repaint();
        }
    }

    public void resetFrame() {
        image = null;
        imageArray = null;
        imageFromArray = null;
    }

    public boolean getImageFromFile(File file) {
        try {
            image = ImageIO.read(file);
            getImageAsArray();
            convertFromArray();
            return true;
        } catch (IOException e) {
            System.out.println("Exception Caught");
            return false;
        }
    }

    private void getImageAsArray() {
        imageArray = new Color[image.getWidth()][image.getHeight()];
        for(int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
            for(int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
                imageArray[xPixel][yPixel] = new Color(image.getRGB(xPixel, yPixel));
            }
        }
    }

    private void convertFromArray() {
        imageFromArray = new BufferedImage(imageArray.length, imageArray[0].length, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < imageArray.length; x++) {
            for(int y = 0; y < imageArray[0].length; y++) {
                imageFromArray.setRGB(x, y, imageArray[x][y].getRGB());
            }
        }
    }

    public void convertToGreyScale() {
        imageFromArray = new BufferedImage(imageArray.length, imageArray[0].length, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < imageArray.length; x++) {
            for(int y = 0; y < imageArray[0].length; y++) {

                Color pixelColor = imageArray[x][y];

                int R = pixelColor.getRed();
                int G = pixelColor.getGreen();
                int B = pixelColor.getBlue();

                double Y = 0.299*R + 0.587*G + 0.114*B;
                double U = 0;
                double V = 0;

                R = (int)(0.946*Y - 0.045*U + 1.009*V);
                G = (int)(Y - 0.166*U - 0.5*V);
                B = (int)(0.982*Y + 1.003*U + 0.009*V);

                Color newPixelColor = new Color(R,G,B);

                imageFromArray.setRGB(x, y, newPixelColor.getRGB());
            }
        }
    }

    public void convertToDither() {
        imageFromArray = new BufferedImage(imageArray.length, imageArray[0].length, BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < imageArray.length; x++) {
            for(int y = 0; y < imageArray[0].length; y++) {

                Color pixelColor = imageArray[x][y];

                int R = pixelColor.getRed();
                int G = pixelColor.getGreen();
                int B = pixelColor.getBlue();

                double Y = 0.299*R + 0.587*G + 0.114*B;

                Color ditherColor;

                double ditherY = Y/( 255/( (ditherMatrix.length * ditherMatrix.length) - 1) );

                if(ditherY < ditherMatrix[x % ditherMatrix.length][y % ditherMatrix.length]) {
                    ditherColor = Color.BLACK;
                }
                else {
                    ditherColor = Color.WHITE;
                }

                imageFromArray.setRGB(x, y, ditherColor.getRGB());
            }
        }
    }

    private void checkPercent(double maxPercentile, double minPercentile, double percent, boolean[] arr, int i) {
        if(percent > maxPercentile || percent < minPercentile) {
            arr[i] = false;
        }
    }

    private void getPercents(int[] countArray, boolean[] inPercentile) {
        for(int i = 0; i < 255; i++) {

            int count = 0;

            for(int j = 0; j < 255; j++) {
                if(countArray[i] > countArray[j]) {
                    count++;
                }
            }

            double redPercent = (double)count / 255;

            checkPercent(0.90, 0.10, redPercent, inPercentile, i);
        }
    }

    public void convertToAutoLevel() {
        imageFromArray = new BufferedImage(imageArray.length, imageArray[0].length, BufferedImage.TYPE_INT_RGB);
        int[] redCountArray = new int[256];
        int[] greenCountArray = new int[256];
        int[] blueCountArray = new int[256];

        int minR = Integer.MAX_VALUE;
        int maxR = Integer.MIN_VALUE;

        int minG = Integer.MAX_VALUE;
        int maxG = Integer.MIN_VALUE;

        int minB = Integer.MAX_VALUE;
        int maxB = Integer.MIN_VALUE;

        //getCounts for RGB
        for(int x = 0; x < imageArray.length; x++) {
            for(int y = 0; y < imageArray[0].length; y++) {
                
                Color pixelColor = imageArray[x][y];

                int R = pixelColor.getRed();
                int G = pixelColor.getGreen();
                int B = pixelColor.getBlue();

                redCountArray[R]++;
                greenCountArray[G]++;
                blueCountArray[B]++;
            }
        }

        boolean[] inRedPercentile = new boolean[256];
        boolean[] inBluePercentile = new boolean[256];
        boolean[] inGreenPercentile = new boolean[256];

        for(int i = 0; i < 256; i++) {
            inRedPercentile[i] = true;
            inBluePercentile[i] = true;
            inGreenPercentile[i] = true;
        }
        
        //get Percents

        getPercents(redCountArray, inRedPercentile);
        getPercents(greenCountArray, inGreenPercentile);
        getPercents(blueCountArray, inBluePercentile);

        for(int x = 0; x < imageArray.length; x++) {
            for(int y = 0; y < imageArray[0].length; y++) {
                
                Color pixelColor = imageArray[x][y];

                int R = pixelColor.getRed();
                int G = pixelColor.getGreen();
                int B = pixelColor.getBlue();

                if(inRedPercentile[R]) {
                    minR = min(R, minR);
                    maxR = max(R, maxR);
                }
                if(inGreenPercentile[G]) {
                    minG = min(G, minG);
                    maxG = max(G, maxG);
                }
                if(inBluePercentile[B]) {
                    minB = min(B, minB);
                    maxB = max(B, maxB);
                }
            }
        }

        //scale
        for(int x = 0; x < imageArray.length; x++) {
            for(int y = 0; y < imageArray[0].length; y++) {

                Color pixelColor = imageArray[x][y];

                int R = pixelColor.getRed();
                int G = pixelColor.getGreen();
                int B = pixelColor.getBlue();

                if(R < minR) {
                    R = minR;
                }
                if(R > maxR) {
                    R = maxR;
                }

                if(G < minG) {
                    G = minG;
                }
                if(G > maxG) {
                    G = maxG;
                }

                if(B < minB) {
                    B = minB;
                }
                if(B > maxB) {
                    B = maxB;
                }

                R = (R - minR) * (255 / (maxR - minR));
                G = (G - minG) * (255 / (maxG - minG));
                B = (B - minB) * (255 / (maxB - minB));
                
                Color newPixelColor = new Color(R,G,B);

                imageFromArray.setRGB(x, y, newPixelColor.getRGB());
            }
        }

        saveImageToFile("auto_levels_image.jpg");
    
    }

    private void saveImageToFile(String filename) {
        try {
            ImageIO.write(imageFromArray, "jpg", new File(filename));
            System.out.println("Image saved successfully as " + filename);
        } catch (IOException e) {
            System.out.println("Error saving image: " + e.getMessage());
        }
    }

    public void convertToNormal() {
        imageFromArray = image;
    }

    private int max(int a, int b) {
        if(a > b) {
            return a;
        }
        return b;
    }

    private int min(int a, int b) {
        if(a < b) {
            return a;
        }
        return b;
    }
}
  