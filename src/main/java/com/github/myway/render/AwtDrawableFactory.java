package com.github.myway.render;

public class AwtDrawableFactory implements DrawableFactory {

	@Override
	public Drawable create(int w, int h) {
		return new AwtDrawable(w, h);
	}

}
