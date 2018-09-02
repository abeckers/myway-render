package com.github.myway.render;

import java.io.IOException;

public interface Drawable {

	void close();

	void drawLine(int x1, int y1, int x2, int y2);

	void setColor(MywayColor white);

	void fillRect(int i, int j, int w, int h);

	void drawString(String text, int x, int y);

	void saveTo(String string) throws IOException;

}
