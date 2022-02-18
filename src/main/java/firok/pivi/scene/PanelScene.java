package firok.pivi.scene;

import firok.pivi.util.TimeUtil;

import javax.swing.*;
import java.awt.*;

public class PanelScene extends JPanel
{
	SceneManager smgr;
	Thread threadRendering;
	public PanelScene(SceneManager smgr)
	{
		super(null);
		this.smgr = smgr;

		this.threadRendering = new Thread(()->{
			while(true)
			{
				try
				{
					this.repaint();
					Thread.sleep(20);
				}
				catch (Exception ignored) { }
			}
		});
		this.threadRendering.setDaemon(true);
		this.threadRendering.start();
	}

	@Override
	public void paint(Graphics g)
	{
		if(g != null)
		{
			final long now = TimeUtil.getNow();
			final int width = getWidth(), height = getHeight();
			var img = smgr.render(width, height, now);
			g.drawImage(img, 0, 0, width, height, (img1, infoflags, x, y, width1, height1) -> false);
		}
	}

}
