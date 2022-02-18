package firok.pivi.scene;

import java.awt.*;

class EntityParticleLoading extends EntityParticleRound
{
	double factorUpSpeed;
	public EntityParticleLoading(int x, int y, int color, int size, double factorUpSpeed)
	{
		super(x, y, color, size);
		this.factorUpSpeed = factorUpSpeed;
	}

	public int currentY(long now)
	{
		int interval = (int)(now - timeCreated);
		if(interval < 0) return Integer.MIN_VALUE;

		return (int)(interval * factorUpSpeed);
	}

	@Override
	public void render(Graphics sceneGraphics, int sceneWidth, int sceneHeight, long now)
	{
		super.y = currentY(now);
		super.render(sceneGraphics, sceneWidth, sceneHeight, now);
	}
}
