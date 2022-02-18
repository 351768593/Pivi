package firok.pivi.scene;

import firok.pivi.Pivi;
import firok.pivi.gui.PiviForm;
import firok.pivi.util.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * 管理渲染内容
 */
public class SceneManager
{
	private final Object LOCK = new Object();

	/**
	 * 将当前场景切换至指定图片的渲染
	 */
	public void switchToImage(URL url)
	{
		;
	}

	/**
	 * 清空当前场景中的图片
	 */
	public void clearAnyImage()
	{
		;
	}

	private Color bgc;

	/**
	 * @return 渲染场景
	 */
	public BufferedImage render(int width, int height, long now)
	{
		if(width <= 0 || height <= 0) return null;
		if(bgc == null) bgc = new Color(55,39,63);

		var ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		synchronized (LOCK)
		{
			var g = ret.createGraphics();
			g.setColor(bgc);

			g.fillRect(0, 0, width, height);
			for(var enScene : listEntity(true))
			{
				enScene.render(g, width, height, now);
			}
		}

		return ret;
	}

	private List<Entity> listSceneEntity;
	public void addEntity(Entity en)
	{
		synchronized (LOCK) { listSceneEntity.add(en); }
	}
	public List<Entity> listEntity(boolean clearDeath)
	{
		synchronized (LOCK) {
			if(clearDeath)
				listSceneEntity.removeIf(Entity::isDead);
			return new ArrayList<>(listSceneEntity);
		}
	}
	public void removeEntityById(UUID uuid)
	{
		synchronized (LOCK)
		{
			listSceneEntity.removeIf(en -> en.isDead() || en.uuid.equals(uuid));
		}
	}

	public void startThread()
	{
		thread = new ThreadSceneLoadingParticle(Pivi.smgr);
		thread.start();
	}

	ThreadSceneLoadingParticle thread;
	public SceneManager()
	{
		listSceneEntity = new LinkedList<>();
	}
}
