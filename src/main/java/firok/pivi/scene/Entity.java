package firok.pivi.scene;

import firok.pivi.util.TimeUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.UUID;

abstract class Entity
{
	/**
	 * 全局id
	 */
	public final UUID uuid = UUID.randomUUID();
	/**
	 * 创建时间
	 */
	public final long timeCreated = TimeUtil.getNow();

	/**
	 * 是否需要被移除
	 */
	private boolean alive = true;
	public final void kill() { alive = false; }
	public final boolean isDead() { return !alive; }

	public abstract void render(Graphics sceneGraphics, int sceneWidth, int sceneHeight, long now);
}
