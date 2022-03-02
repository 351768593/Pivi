package firok.pivi.gui;

import firok.pivi.config.ConfigBean;
import firok.pivi.config.ConfigZoomMode;
import firok.pivi.util.Colors;
import firok.pivi.util.ResourceUtil;

import javax.swing.*;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.Objects;

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
//	public String imageType;
	public EnumLoadingStatus status;
	public Exception exception;
	public Image image;
	public int imageWidth, imageHeight;
	public ImageObserver iob;

	public int viewportLocX, viewportLocY;
	public int viewportPercent;
	public int viewportDragPreX, viewportDragPreY;
	public boolean isViewportDragging;
	public boolean isAnimatedImage;
	public boolean needRepainting;
	public boolean needRepaint()
	{
		return ( // 判断动画属性和图片属性
			this.isAnimatedImage ||
			this.needRepainting
		) && ( // 判断窗体状态
			this.frame != null &&
			this.frame.isFocused() &&
			this.frame.isVisible()
		);
	}

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
								g.drawImage(image, viewportLocX, viewportLocY, imageWidth, imageHeight, iob);

								g.setColor(colorCommon);
								g.drawString(urlString, 0, size);
								g.drawString(imageWidth + " × " + imageHeight, 0, size * 2 + 4);
//								g.drawString(imageType, 0, size * 3 + 8);
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

		menu = new FramePopupMenu();

		pViewport.setComponentPopupMenu(menu);
		vml = this.new ViewportMouseListener();
		pViewport.addMouseListener(vml);
		pViewport.addMouseWheelListener(vml);
		pViewport.addMouseMotionListener(vml);
	}
	private class FramePopupMenu extends JPopupMenu
	{
		final JMenuItem miCopyFullURL;
		final JMenuItem miCopyFullShort;

		final JMenuItem miImageFitAuto;
		final JMenuItem miImageFitHeight;
		final JMenuItem miImageFitWidth;
		final JMenuItem miImageOriginSize;
		FramePopupMenu()
		{
			super();
			miCopyFullURL = new JMenuItem("复制完整URL");
			miCopyFullURL.addActionListener(e -> putTextIntoClipboard(PiviImageForm.this.urlString));
			add(miCopyFullURL);

			miCopyFullShort = new JMenuItem("复制文件名");
			miCopyFullShort.addActionListener(e -> putTextIntoClipboard(PiviImageForm.this.urlShort));
			add(miCopyFullShort);


			miImageFitAuto = new JMenuItem("自动适应窗口大小");
			miImageFitAuto.addActionListener(e -> adjustImageZoom(ConfigZoomMode.FitAuto));
			add(miImageFitAuto);

			miImageFitWidth = new JMenuItem("适应窗口宽度");
			miImageFitWidth.addActionListener(e -> adjustImageZoom(ConfigZoomMode.FitWindowWidth));
			add(miImageFitWidth);

			miImageFitHeight = new JMenuItem("适应窗口高度");
			miImageFitHeight.addActionListener(e -> adjustImageZoom(ConfigZoomMode.FitWindowHeight));
			add(miImageFitHeight);

			miImageOriginSize = new JMenuItem("原始大小");
			miImageOriginSize.addActionListener(e -> adjustImageZoom(ConfigZoomMode.OriginSize));
			add(miImageOriginSize);


		}

		static void putTextIntoClipboard(String value)
		{
			var cp = Toolkit.getDefaultToolkit().getSystemClipboard();
			var trans = new StringSelection(value);
			cp.setContents(trans, null);
		}
		static void adjustImageZoom(ConfigZoomMode mode)
		{
			;
		}
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
				needRepainting = true;
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
				needRepainting = true;
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
				var _u = ResourceUtil.readUrl(raw);
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
						PiviImageForm.this.urlShort = _ius > 0 ? _us.substring(_ius + 1) : _us;
						PiviImageForm.this.status = EnumLoadingStatus.Started;
					}

					var bytes = ResourceUtil.readBytes(_u);
					var _isAnimatedImage = ResourceUtil.isAnimatedImage(bytes);
					var _ii = new ImageIcon(bytes);
					var _i = _ii.getImage();
					if(_i == null)
					{
						throw new RuntimeException("无法读取为图片: "+_u);
					}

					synchronized (LOCK)
					{
						PiviImageForm.this.status = EnumLoadingStatus.Finished;
						PiviImageForm.this.image = _i;
						PiviImageForm.this.isAnimatedImage = _isAnimatedImage;
						PiviImageForm.this.iob = _ii.getImageObserver();
						PiviImageForm.this.imageWidth = _i.getWidth(PiviImageForm.this.iob);
						PiviImageForm.this.imageHeight = _i.getHeight(PiviImageForm.this.iob);
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
	// fixme low 改成多个frame共用一个线程的写法 (协程)
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
						if(PiviImageForm.this.needRepaint())
						{
							PiviImageForm.this.pViewport.repaint();
							PiviImageForm.this.needRepainting = false;
						}
					}
					Thread.sleep(25);
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
