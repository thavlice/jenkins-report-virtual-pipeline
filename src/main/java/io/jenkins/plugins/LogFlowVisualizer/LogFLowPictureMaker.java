/*
 * The MIT License
 *
 * Copyright 2023
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.jenkins.plugins.LogFlowVisualizer;

import io.jenkins.plugins.LogFlowVisualizer.model.LineOutput;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class LogFLowPictureMaker {

    private final int width;
    private int height;
    private Font font = new Font("Arial", Font.PLAIN, 20);
    private Color backColor = Color.WHITE;
    private Color textColor = Color.BLACK;

    public LogFLowPictureMaker(int width, int height, Font font, Color backColor, Color textColor) {
        this.width = width;
        this.height = height;
        this.font = font;
        this.backColor = backColor;
        this.textColor = textColor;
    }

    public LogFLowPictureMaker(int width, int height) {
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

    public BufferedImage createPicture(List<LineOutput> lines) {
        int toDisplayCounter = 0;
        for (LineOutput line :
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
        for (LineOutput line : lines) {
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
