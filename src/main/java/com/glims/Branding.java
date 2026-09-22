package com.glims;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class Branding {
    BufferedImage upLogo, lgnUPLogoResized, rbbnUPLogoResized, borrowIcon, returnIcon, arrowIcon;
    Color darkermaroon, maroon, lightgray, white, gray, lightergray, yellow; 
    Font sizedFontPalatinoBig, sizedFontPalatinoSmall, sizedFontRobotoBold15;

    public Branding(){
        maroon = new Color(94,38,5);
        darkermaroon = new Color(60,28,14);
        lightgray = new Color(238, 224, 229);
        lightergray = new Color(242, 242, 242);
        gray = new Color(200, 200, 200); 
        white = new Color(255, 255, 255);
        yellow = new Color(254, 183, 28);

        try {
            Font palatinoFont = Font.createFont(Font.TRUETYPE_FONT, new File("Assets/Font/Palatino.ttf"));
            sizedFontPalatinoBig = palatinoFont.deriveFont(Font.PLAIN, 35);
            sizedFontPalatinoSmall = palatinoFont.deriveFont(Font.PLAIN, 27);
            sizedFontRobotoBold15 = new Font("Roboto", Font.BOLD, 15);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

        try {
            upLogo = ImageIO.read(new File("Assets/Logo/UP Logo.png"));
            borrowIcon = ImageIO.read(new File("Assets/Icons/borrow.png"));
            returnIcon = ImageIO.read(new File("Assets/Icons/return.png"));
            arrowIcon = ImageIO.read(new File("Assets/Icons/arrow.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        lgnUPLogoResized = resizeImage(upLogo, 150, 150);
        rbbnUPLogoResized = resizeImage(upLogo, 110, 110);
        borrowIcon = resizeImage(borrowIcon, 35, 45);
        returnIcon = resizeImage(returnIcon, 35, 45);  
        arrowIcon = resizeImage(arrowIcon, 30,30);

        UIManager.put("OptionPane.background", lightgray);
        UIManager.put("Panel.background", lightgray);
        UIManager.put("OptionPane.messageForeground", maroon);
        UIManager.put("Button.background", maroon);
        UIManager.put("Button.foreground", white);
        UIManager.put("Label.font", new Font("Roboto", Font.PLAIN, 15));
        UIManager.put("Button.font", new Font("Roboto", Font.PLAIN, 15));
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    public String reformatDateLabel(String dateString) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Define the output formatter for the desired format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
        
        try {
            // Parse the input string to a LocalDateTime object
            LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);
            
            // Format the LocalDateTime object to the desired output format
            return dateTime.format(outputFormatter);
        } catch (Exception e) {
            // Return original string or error message if parsing fails
            return "Invalid date format: " + dateString;
        }
    }

    public void setAppIcon(JFrame frame){
        try {
            BufferedImage originalIcon = ImageIO.read(new File("Assets/Logo/UP Logo.png"));
            // Resize the icon (example size: 32x32)
            BufferedImage resizedIcon = resizeImage(originalIcon, 32, 32);
            frame.setIconImage(resizedIcon);
        } catch (IOException e) {
            System.out.println("Icon image not found!");
            e.printStackTrace();
        }
    }

    public void reskinScrollBar(JScrollPane scrollPane, Color color){
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    if (color.equals(gray)){
                        thumbColor = gray;
                        trackColor = lightgray;
                    }else if (color.equals(maroon)) {
                        thumbColor = darkermaroon;
                        trackColor = maroon;
                    }
                }

                @Override
                protected JButton createDecreaseButton(int orientation) {
                    return createZeroButton();
                }

                @Override
                protected JButton createIncreaseButton(int orientation) {
                    return createZeroButton();
                }

                private JButton createZeroButton() {
                    JButton button = new JButton();
                    button.setPreferredSize(new Dimension(0, 0));
                    button.setMinimumSize(new Dimension(0, 0));
                    button.setMaximumSize(new Dimension(0, 0));
                    return button;
                }
        });
        
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(12, Integer.MAX_VALUE)); // 12px width

        JScrollBar horizontalBar = scrollPane.getHorizontalScrollBar();
        horizontalBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 12)); // 12px height    
    }
}
