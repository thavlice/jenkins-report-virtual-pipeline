package io.jenkins.plugins.VirtualPipeline;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class VirtualPipelinePictureMaker {

    private final int width;
    private int height;
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

    public Font getFont() {
        return font;
    }

    public Color getBackColor() {
        return backColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public BufferedImage createPicture(List<VirtualPipelineLineOutput> lines) {
        int toDisplayCounter = 0;
        for (VirtualPipelineLineOutput line :
                lines) {
            if (line.getDisplay()) {
                toDisplayCounter += 1;
            }
        }
        this.setHeight((5 + toDisplayCounter) * (font.getSize())); //setting size according to number of displayed lines

        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(this.getBackColor());
        graphics2D.fillRect(0, 0, this.getWidth(), this.getHeight());
        graphics2D.setColor(this.getTextColor());
        graphics2D.setFont(this.getFont());

        int lineHeight = graphics2D.getFontMetrics().getHeight();
        int y = 25;
        for (VirtualPipelineLineOutput line : lines) {
            if (!line.getDisplay()) {
                continue;
            }
            graphics2D.setColor(this.getTextColor());
            switch (line.getType()) {
                case ONE_LINE:
                    graphics2D.setColor(Color.green);
                    break;
                case START_MARK:
                    graphics2D.setColor(Color.darkGray);
                    break;
                case END_MARK:
                    graphics2D.setColor(Color.lightGray);
                    break;
                case LIMIT_REACHED_LINE:
                    graphics2D.setColor(Color.orange);
                    break;
                case CONTENT_LINE:
                    graphics2D.setColor(Color.blue);
                    break;
                default:
                    graphics2D.setColor(textColor);
                    break;
            }
            //int x = (width - graphics2D.getFontMetrics().stringWidth(line.getLine())) / 2;
            int x = 5;
            graphics2D.drawString(line.getIndex() + " " + line.getLine(), x, y);
            y += lineHeight;
        }
        return image;

    }
}
