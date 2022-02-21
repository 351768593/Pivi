package firok.pivi.gui;

import java.awt.image.BufferedImage;

public class ViewportState
{
	public BufferedImage image;
	public int imageWidth, imageHeight;

	public int viewportPercent;

	public int getViewportWidth()
	{
		return (int) (0.01f * viewportPercent * imageWidth);
	}
	public int getViewportHeight()
	{
		return (int) (0.01f * viewportPercent * imageHeight);
	}

	public int viewportLocX, viewportLocY;
}
