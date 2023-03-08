package io.jenkins.plugins.VirtualPipeline;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class VirtualPipelinePictureMaker {

    private final int width;
    private final int height;
    private Font font = new Font("Arial", Font.PLAIN, 20);
    private Color backColor = Color.WHITE;
    private Color textColor = Color.BLACK;

    public VirtualPipelinePictureMaker(int width, int height, Font font, Color backColor, Color textColor) {
        this.width = width;
        this.height = height;
        this.font = font;
        this.backColor = backColor;
        this.textColor = textColor;
    }

    public VirtualPipelinePictureMaker(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public BufferedImage createPicture(List<String> lines) {

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(backColor);
        graphics2D.fillRect(0, 0, width, height);
        graphics2D.setColor(textColor);
        graphics2D.setFont(font);

        int lineHeight = graphics2D.getFontMetrics().getHeight();
        int y = (height - lineHeight * lines.size()) / 2;
        for (String lineText : lines) {
            int x = (width - graphics2D.getFontMetrics().stringWidth(lineText)) / 2;
            graphics2D.drawString(lineText, x, y);
            y += lineHeight;
        }
        return image;

    }
}
