package com.github.myway.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class AwtDrawable implements Drawable {

	private BufferedImage img;
	private Graphics g;

	public AwtDrawable(int w, int h) {
		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		g = img.getGraphics(); // TODO Auto-generated method stub
	}

	@Override
	public void close() {
		g.dispose();
	}

	private Color convertColor(MywayColor color) {
		switch (color) {
		case WHITE:
			return Color.WHITE;
		case BLUE:
			return Color.BLUE;
		case CYAN:
			return Color.CYAN;
		case GREEN:
			return Color.GREEN;
		case RED:
			return Color.RED;

		default:
			return Color.BLACK;
		}
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		g.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void fillRect(int x, int y, int w, int h) {
		g.fillRect(x, y, w, h);
	}

	@Override
	public void setColor(MywayColor color) {
		g.setColor(convertColor(color));
	}

	@Override
	public void drawString(String text, int x, int y) {
		g.drawString(text, x, y);
	}

	@Override
	public void saveTo(String filename) throws IOException {
		ImageIO.write(img, "jpg", new File(filename));

	}

}
