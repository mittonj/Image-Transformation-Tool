import javax.swing.*;
import java.io.File;
import java.awt.*;

public class bmpUI {
    private JFrame frame;
    private JButton fileChooserButton;
    private JButton exitButton;
    private JButton nextButton;
    private File imageFile;
    private JPanel imagePanel;
    private JPanel northPanel;
    private JPanel southPanel;
    private imagePanel leftImage = null;
    private imagePanel rightImage = null;

    private enum States {
        GREY,
        DITHER,
        AUTOLEVELS
    }

    private States state;

    public bmpUI() {
        createFrame();
        createImagePanel();
        createLeftImage();
        createRightImage();
        createNorthPanel();
        createSouthPanel();
        createFileChooserButton();
        createCloseButton();
        createNextButton();

        frame.setVisible(true);

        imagePanel.revalidate();
    }

    private void createFrame() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setSize(200,200);
        frame.setLocation(200,200);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createFileChooserButton() {
        fileChooserButton = new JButton();
        fileChooserButton.setText("Open File");

        fileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // JFileChooser code (next 6 lines) found at https://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

                int result = fileChooser.showOpenDialog(frame);

                if(result == JFileChooser.APPROVE_OPTION) {

                    state = States.DITHER;

                    leftImage.resetFrame();
                    rightImage.resetFrame();

                    imageFile = fileChooser.getSelectedFile();
                                      
                    leftImage.getImageFromFile(imageFile);
                    rightImage.getImageFromFile(imageFile);

                    leftImage.displayImage();

                    rightImage.convertToGreyScale();

                    rightImage.displayImage();


                    frame.pack();
                }
            }
        });

        northPanel.add(fileChooserButton);
    }

    private void createCloseButton() {
        exitButton = new JButton();
        exitButton.setText("Exit");

        exitButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.exit(0);
            }
        });

        northPanel.add(exitButton);
    }

    private void createNextButton() {
        nextButton = new JButton();
        nextButton.setText("Next ->");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if(leftImage.isNull() || rightImage.isNull()) {
                    return;
                }

                if(state == States.GREY) {
                    leftImage.convertToNormal();
                    rightImage.convertToGreyScale();
                    state = States.DITHER;
                }

                else if(state == States.DITHER) {
                    leftImage.convertToGreyScale();
                    rightImage.convertToDither();
                    state = States.AUTOLEVELS;
                }

                else {
                    leftImage.convertToNormal();
                    rightImage.convertToAutoLevel();
                    state = States.GREY;
                }
                leftImage.displayImage();
                rightImage.displayImage();
                frame.pack();
            }
        });
        southPanel.add(nextButton);
    }
    
    private void createImagePanel() {
        GridLayout imageGrid = new GridLayout(0,2);
        imagePanel = new JPanel();
        imagePanel.setLayout(imageGrid);
        frame.add(imagePanel, BorderLayout.CENTER);
    }

    private void createNorthPanel() {
        GridLayout northGrid = new GridLayout(0,2);
        northPanel = new JPanel();
        northPanel.setLayout(northGrid);
        frame.add(northPanel, BorderLayout.NORTH);
    }

    private void createSouthPanel() {
        GridLayout southGrid = new GridLayout(0,1);
        southPanel = new JPanel();
        southPanel.setLayout(southGrid);
        frame.add(southPanel, BorderLayout.SOUTH);
    }

    private void createLeftImage() {
        leftImage = new imagePanel();
        imagePanel.add(leftImage);
    }

    private void createRightImage() {
        rightImage = new imagePanel();
        imagePanel.add(rightImage);
    }
}
