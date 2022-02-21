package firok.pivi.scene;

import firok.pivi.Pivi;
import firok.pivi.config.ConfigZoomMode;
import firok.pivi.gui.PiviForm;
import firok.pivi.gui.ViewportState;
import firok.pivi.gui.ViewportSwitch;
import firok.pivi.util.Colors;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
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
	 * 静态viewport
	 */
	private ViewportState stateStatic;
	/**
	 * 正在进行动画的viewport
	 */
	private ViewportSwitch stateAnimation;
	/**
	 * viewport是否正在进行动画
	 */
	private boolean isStateAnimated;

	/**
	 * @param vs 新的viewport
	 * @param immediately 无动画 直接切换
	 */
	public void switchViewport(ViewportState vs, boolean immediately)
	{
		synchronized (LOCK)
		{
			if(immediately)
			{
				stateStatic = vs;
				isStateAnimated = false;
			}
			else
			{
				
				isStateAnimated = true;
			}
		}
	}


	/**
	 * 将当前场景切换至指定图片的渲染
	 */
	public void switchToImage(URL url)
	{
		Thread th = new Thread(()->{
			try
			{
				var image = ImageIO.read(url);
				var imageWidth = image.getWidth();
				var imageHeight = image.getHeight();
				System.out.println("图片加载完成");
			}
			catch (Exception e)
			{
				System.out.println("打开url失败");
				e.printStackTrace();
			}
		});
		th.setDaemon(true);
		th.start();
	}

	/**
	 * 清空当前场景中的图片
	 */
	public void clearAnyImage()
	{
		;
	}

	private Color bgc;

	ImageObserver ob = (img, infoflags, x, y, width1, height1) -> false;

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

			if(image != null)
			{
				ConfigZoomMode mode = Pivi.config.getInitZoomMode();
				switch(mode)
				{
					case OriginSize:
					{
						g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), ob);
						break;
					}
					case FitWindowWidth: case FitWindowHeight:
					{
						double ratio = 1d * imageHeight / imageWidth;
						int renderWidth = mode == ConfigZoomMode.FitWindowWidth ? width : (int)(1d * height / imageHeight  * imageWidth);
						int renderHeight = mode == ConfigZoomMode.FitWindowHeight ? height : (int)(1d * width / imageWidth * imageHeight);
						g.drawImage(image, 0, 0, renderWidth, renderHeight, ob);
						break;
					}
					case CustomPercent:
						break;
				}

			}

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
