package firok.pivi.gui;

import lombok.Getter;

import java.awt.image.BufferedImage;

@Getter
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

	/**
	 * 这个坐标是基于panel的位置的
	 */
	public int viewportLocX, viewportLocY;
}
