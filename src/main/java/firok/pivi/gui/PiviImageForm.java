package firok.pivi.gui;

import firok.pivi.config.ConfigBean;
import firok.pivi.util.Colors;
import firok.pivi.util.URLUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;

public class PiviImageForm
{
	public JFrame frame;
	public JPanel pViewport;
	public JPopupMenu menu;

	public final Object LOCK = new Object();
	public URL url;
	public EnumLoadingStatus status;
	public Exception exception;
	public BufferedImage image;
	public int imageWidth, imageHeight;
	public int viewportLocX, viewportLocY;
	public int viewportPercent;
	public int getViewportWidth()
	{
		return (int)(.01f * viewportPercent * imageWidth);
	}
	public int getViewportHeight()
	{
		return (int)(.01f * viewportPercent * imageHeight);
	}

	public void initFromConfig(ConfigBean config)
	{
		;
	}

	private static final ImageObserver ob = (img, infoflags, x, y, width, height) -> false;

	private static Font font;
	private static Color colorCommon = new Color(Colors.CadetBlue);
	private static Color colorError = new Color(Colors.OrangeRed);
	private static Color colorOther = new Color(Colors.GreenYellow);
	private void createUIComponents()
	{
		pViewport = new JPanel(){
			@Override
			public void paint(Graphics g)
			{
				g = g == null ? this.getGraphics() : g;

				if(g != null)
				{
					synchronized (LOCK)
					{
						if(font == null)
							font = UIManager.getFont("defaultFont");
						g.setFont(font);
						final var size = font.getSize();

						if(status != null)
						switch (status)
						{
							case Finished -> {
								g.drawImage(image, 0, 0, imageWidth, imageHeight, ob);

								g.setColor(colorCommon);
								g.drawString(imageWidth + " × " + imageHeight, 0, size);
								g.drawString("测试文本 123 test ABC", 0, size * 2 + 2);
							}
							case Error -> {
								var message = exception.getMessage();
								g.setColor(colorError);
								g.drawString(message, 0, size);
								int c = size * 2 + 2;
								for(var st : exception.getStackTrace())
								{
									g.drawString(st.getClassName() + " # " + st.getMethodName(), size / 2, c);
									c += size + 2;
								}
							}
						}
					}

					g.dispose();
				}

			}
		};

		menu = new JPopupMenu("测试menu");
		menu.add("123");
		menu.add("456");

		pViewport.setComponentPopupMenu(menu);
	}

	private Thread threadLoad;
	public void startLoading(String raw)
	{
		threadLoad = new Thread(()->{
			PiviImageForm.this.status = EnumLoadingStatus.NotStarted;
			try
			{
				var _u = URLUtil.readUrl(raw);
				if(_u == null)
				{
					synchronized (LOCK)
					{
						PiviImageForm.this.url = null;
						PiviImageForm.this.status = EnumLoadingStatus.Error;
						PiviImageForm.this.exception = new Exception("无法识别路径: "+raw);
						PiviImageForm.this.image = null;
					}
				}
				else
				{
					synchronized (LOCK)
					{
						PiviImageForm.this.url = _u;
						PiviImageForm.this.status = EnumLoadingStatus.Started;
					}

					var _i = ImageIO.read(_u);
					if(_i == null)
					{
						throw new RuntimeException("无法读取为图片: "+_u);
					}

					synchronized (LOCK)
					{
						PiviImageForm.this.status = EnumLoadingStatus.Finished;
						PiviImageForm.this.image = _i;
						PiviImageForm.this.imageWidth = _i.getWidth();
						PiviImageForm.this.imageHeight = _i.getHeight();
					}

					PiviImageForm.this.pViewport.repaint();
				}
			}
			catch (Exception e)
			{
				synchronized (LOCK)
				{
					PiviImageForm.this.status = EnumLoadingStatus.Error;
					PiviImageForm.this.exception = e;
				}
			}
		});
		threadLoad.start();
	}
	public void stopLoading()
	{
		if(threadLoad != null)
			threadLoad.interrupt();
	}
}
