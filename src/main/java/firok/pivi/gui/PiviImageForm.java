package firok.pivi.gui;

import firok.pivi.config.ConfigBean;
import firok.pivi.config.ConfigZoomMode;
import firok.pivi.util.Colors;
import firok.pivi.util.URLUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.Objects;

import static java.awt.image.BufferedImage.*;

public class PiviImageForm
{
	public JFrame frame;
	public JPanel pViewport;
	public JPopupMenu menu;
	public ViewportMouseListener vml;



	public final Object LOCK = new Object();
	public URL url;
	public String urlString;
	public String urlShort;
	public String imageType;
	public EnumLoadingStatus status;
	public Exception exception;
	public BufferedImage image;
	public int imageWidth, imageHeight;

	public int viewportLocX, viewportLocY;
	public int viewportPercent;
	public int viewportDragPreX, viewportDragPreY;
	public boolean isViewportDragging;
	public boolean needRepainting;

	ConfigZoomMode zoomMode;
	int zoomPercent;
	public void initFromConfig(ConfigBean config)
	{
		zoomMode = config.getInitZoomMode();
		zoomPercent = config.getInitZoomPercent();
	}

	public long whenStartImageInformation = -1;
	public int getViewportWidth()
	{
		return (int)(.01f * viewportPercent * imageWidth);
	}
	public int getViewportHeight()
	{
		return (int)(.01f * viewportPercent * imageHeight);
	}

	private static final ImageObserver ob = (img, infoflags, x, y, width, height) -> false;

	private static Font font;
	private static Color colorCommon = new Color(Colors.CadetBlue);
	private static Color colorError = new Color(Colors.OrangeRed);
	private static Color colorOther = new Color(Colors.GreenYellow);
	private static Color colorBackground = Objects.requireNonNullElse(UIManager.getColor("background"), new Color(Colors.WhiteSmoke));
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
						final int graphicWidth = this.getWidth(), graphicHeight = this.getHeight();
						if(font == null)
							font = UIManager.getFont("defaultFont");
						g.setFont(font);
						final var size = font.getSize();

						if(status != null)
						switch (status)
						{
							case Finished -> {
								g.setColor(colorBackground);
								g.fillRect(0, 0, graphicWidth, graphicHeight);
								g.drawImage(image, viewportLocX, viewportLocY, imageWidth, imageHeight, ob);

								g.setColor(colorCommon);
								g.drawString(urlString, 0, size);
								g.drawString(imageWidth + " × " + imageHeight, 0, size * 2 + 4);
								g.drawString(imageType, 0, size * 3 + 8);
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
		menu.add("显示详细信息");
		menu.add("(无二维码)");
		menu.add("保存至 qsave");
		menu.add("保存至 我的文档");

		pViewport.setComponentPopupMenu(menu);
		vml = this.new ViewportMouseListener();
		pViewport.addMouseListener(vml);
		pViewport.addMouseWheelListener(vml);
		pViewport.addMouseMotionListener(vml);
	}

	private class ViewportMouseListener extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			synchronized (LOCK)
			{
				final int button = e.getButton(), clickCount = e.getClickCount();
				if(button == MouseEvent.BUTTON1 && clickCount == 1)
				{
					isViewportDragging = true;
				}
				else
				{
					isViewportDragging = false;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			synchronized (LOCK)
			{
				isViewportDragging = false;
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			synchronized (LOCK)
			{
				final int movement = e.getWheelRotation();
				if(movement < 0) // up
				{
					;
				}
				else if(movement > 0) // down
				{
					;
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			synchronized (LOCK)
			{
				if(!isViewportDragging) return;

				int currentX = e.getX(), currentY = e.getY();
				int iX = currentX - viewportDragPreX, iY = currentY - viewportDragPreY;
				viewportLocX += iX;
				viewportLocY += iY;
				viewportDragPreX = currentX;
				viewportDragPreY = currentY;
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			synchronized (LOCK)
			{
				if(!isViewportDragging)
				{
					viewportDragPreX = e.getX();
					viewportDragPreY = e.getY();
				}
			}
		}
	}

	private Thread threadLoad;
	private Thread threadAnimation;
	public void startLoading(String raw)
	{
		threadLoad = new ThreadLoading(raw);
		threadLoad.start();
		threadAnimation = new ThreadAnimation();
		threadAnimation.start();
	}
	public void stopLoading()
	{
		if(threadLoad != null)
			threadLoad.interrupt();
	}

	private class ThreadLoading extends Thread
	{
		String raw;
		ThreadLoading(String raw)
		{
			super();
			setDaemon(true);
			this.raw = raw;
		}

		@Override
		public void run()
		{
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
						var _us = _u.toString();
						PiviImageForm.this.urlString = _us;
						var _ius = -1;
						_ius = (_ius = _us.lastIndexOf('/')) > 0 ? _ius : _us.lastIndexOf('\\');
						PiviImageForm.this.urlShort = _ius > 0 ? _us.substring(_ius) : _us;
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
						PiviImageForm.this.imageType = switch (image.getType())
								{
									case TYPE_CUSTOM -> "CUSTOM";
									case TYPE_INT_ARGB -> "INT ARGB";
									case TYPE_INT_ARGB_PRE -> "INT ARGB PRE";
									case TYPE_INT_BGR -> "INT BGR";
									case TYPE_INT_RGB -> "INT RGB";
									case TYPE_3BYTE_BGR -> "3 BYTE BGR";
									case TYPE_4BYTE_ABGR -> "4 BYTE ABGR";
									case TYPE_4BYTE_ABGR_PRE -> "4 BYTE ABGR PRE";
									case TYPE_BYTE_BINARY -> "BYTE BINARY";
									case TYPE_BYTE_GRAY -> "BYTE GRAY";
									case TYPE_BYTE_INDEXED -> "BYTE INDEXED";
									case TYPE_USHORT_555_RGB -> "USHORT 555 RGB";
									case TYPE_USHORT_565_RGB -> "USHORT 565 RGB";
									case TYPE_USHORT_GRAY -> "USHORT GRAY";
									default -> "unknow";
								};
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
		}
	}
	private class ThreadAnimation extends Thread
	{
		ThreadAnimation()
		{
			super();
			setDaemon(true);
		}

		@Override
		public void run()
		{
			do
			{
				try
				{
					synchronized (PiviImageForm.this.LOCK)
					{
						if(PiviImageForm.this.needRepainting)
						{
							PiviImageForm.this.pViewport.repaint();
							PiviImageForm.this.needRepainting = false;
						}
					}
					Thread.sleep(20);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					break;
				}
			}
			while (true);
		}
	}
}
