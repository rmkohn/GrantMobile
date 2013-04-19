package com.example.grantmobile.CalendarSquare;

import android.graphics.Color;

public class SquareColors {

	private SquareColors() { }
	
	// completely random
	public static final int NONGRANT_LEAVE_COLOR = 0xff3e29cd;
	public static final int GRANT_COLOR = 0xff04eaa5;
	public static final int DEFAULT_COLOR = Color.TRANSPARENT;
	
	public static final int blendColors(int a, int b) {
		return dissolveColor(a) + dissolveColor(b);
	}
	
	public static final int setAlpha(int c, int alpha) {
		return c & ((alpha << 24) | 0x00ffffff);
	}
	
	public static final int dissolveColor(int c) {
		return (c & 0xfefefefe) >> 1;
	}
	
	public static final int getHighlightColor(int grantHours, int nonGrantHours, int leaveHours) {
		int grantColor = grantHours == 0 ? DEFAULT_COLOR : GRANT_COLOR;
		int otherColor = nonGrantHours + leaveHours == 0 ? DEFAULT_COLOR : NONGRANT_LEAVE_COLOR;
		return setAlpha(dissolveColor(grantColor)
		              + dissolveColor(otherColor)
		              , 0x30);
	}
	
}
