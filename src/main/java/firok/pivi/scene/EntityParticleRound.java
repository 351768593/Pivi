package firok.pivi.scene;

import java.awt.*;
import java.awt.image.BufferedImage;

class EntityParticleRound extends Entity
{
	/**
	 * 当前位置
	 * */
	int x, y;

	/**
	 * 粒子颜色
	 * */
	Color color;

	int half;

	public EntityParticleRound(int x, int y, int color, int size)
	{
		this.x = x;
		this.y = y;
		this.color = new Color(color);
		this.half = size / 2;
		if(size <= 1) kill();
	}

	@Override
	public void render(Graphics sceneGraphics, int sceneWidth, int sceneHeight, long now)
	{
		if(x < -half || y < -half || x > sceneWidth + half || y > sceneHeight + half) kill();
		if(isDead()) return;

		sceneGraphics.setColor(color);
		sceneGraphics.fillOval(sceneWidth - x, sceneHeight - y, half, half);
	}
}
