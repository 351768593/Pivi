package firok.pivi.gui;

import firok.pivi.Pivi;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Objects;

public class PiviImageForm
{
	public JFrame frame;
	public JPanel pViewport;
	public JPopupMenu menu;
	public ViewportMouseListener vml;
	public ViewportKeyListener vkl;

	/**
	 * 多线程数据锁
	 */
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

	/**
	 * 是否正在拖动视角
	 */
	public boolean isViewportDragging;
	/**
	 * 视角是否被拖动过, 用于决定窗口更改大小时是否会改变视角位置
	 */
	public boolean isViewportDragged;
	/**
	 * 前次鼠标所在位置, 用于拖拽视角的坐标计算
	 */
	public int viewportDragPreX, viewportDragPreY;
	/**
	 * 当前视角偏移量
	 */
	public int viewportLocX, viewportLocY;
	/**
	 * 当前视角渲染百分比
	 */
	public BigDecimal viewportPercent;
	/**
	 * 当前图片是否为动图
	 */
	public boolean isAnimatedImage;
	/**
	 * 是否处于Shift模式
	 */
	public boolean isShiftMode;
	/**
	 * 是否处于Alt模式
	 */
	public boolean isAltMode;
	/**
	 * 当前是否需要重绘
	 */
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

	public static final BigDecimal B100 = new BigDecimal(100);

	/**
	 * 根据指定的填充模式设定渲染参数
	 */
	private void adjustImageZoom(ConfigZoomMode mode, BigDecimal extraViewportPercent)
	{
		final int panelWidth = pViewport.getWidth(), panelHeight = pViewport.getHeight();

		zoomMode = mode;
		needRepainting = true;
		isViewportDragged = false;

		if(imageWidth != 0 && imageHeight != 0)
		{
			// 计算缩放百分比
			viewportPercent = switch (mode)
			{
				case OriginSize -> B100;
				case FitWindowWidth -> B100.multiply(new BigDecimal(panelWidth))
						.divide(new BigDecimal(imageWidth), RoundingMode.CEILING);
				case FitWindowHeight -> B100.multiply(new BigDecimal(panelHeight))
						.divide(new BigDecimal(imageHeight), RoundingMode.CEILING);
				case FitAuto -> panelHeight > panelWidth ?
						B100.multiply(new BigDecimal(panelWidth))
								.divide(new BigDecimal(imageWidth), RoundingMode.CEILING) :
						B100.multiply(new BigDecimal(panelHeight))
								.divide(new BigDecimal(imageHeight), RoundingMode.CEILING);
				default -> Objects.requireNonNullElse(extraViewportPercent, B100); // 目前只有自定义缩放大小才会用到这个参数
			};
			// 计算视角位置
			final int vpWidth = getViewportWidth(), vpHeight = getViewportHeight();
			viewportLocX = vpWidth > panelWidth ?
					0 :
					(panelWidth - vpWidth) / 2;
			viewportLocY = vpHeight > panelHeight ?
					0 :
					(panelHeight - vpHeight) / 2;
		}
		else
		{
			viewportPercent = B100;
			viewportLocX = 0;
			viewportLocY = 0;
		}
	}

	ConfigZoomMode zoomMode;
	BigDecimal zoomPercent;
	private void initFromConfig(ConfigBean config)
	{
		zoomMode = config.getInitZoomMode();

		zoomPercent = B100;
		viewportPercent = B100;
	}

	public long whenStartImageInformation = -1;
	public int getViewportWidth()
	{
		// todo 可能需要改成 BigDecimal 计算方式
		return (int)(.01f * viewportPercent.floatValue() * imageWidth);
	}
	public int getViewportHeight()
	{
		return (int)(.01f * viewportPercent.floatValue() * imageHeight);
	}

	private static Font font;
	private static Color colorCommon = new Color(Colors.CadetBlue);
	private static Color colorError = new Color(Colors.OrangeRed);
	private static Color colorOther = new Color(Colors.GreenYellow);
	private static Color colorBackground = Objects.requireNonNullElse(UIManager.getColor("background"), new Color(Colors.WhiteSmoke));
	private void createUIComponents()
	{
		pViewport = new PanelRenderImage();

		menu = new FramePopupMenu();

		pViewport.setComponentPopupMenu(menu);
		vml = this.new ViewportMouseListener();
		pViewport.addMouseListener(vml);
		pViewport.addMouseWheelListener(vml);
		pViewport.addMouseMotionListener(vml);
		vkl = this.new ViewportKeyListener();
	}

	public void postInit(ConfigBean config, JFrame frame)
	{
		this.initFromConfig(config);
		this.frame = frame;
		frame.addKeyListener(vkl);
	}

	private class PanelRenderImage extends JPanel
	{
		public PanelRenderImage()
		{
			super();

			addComponentListener(new ComponentAdapter()
			{
				@Override
				public void componentResized(ComponentEvent e)
				{
					if(!isViewportDragged)
						PiviImageForm.this.adjustImageZoom(PiviImageForm.this.zoomMode, PiviImageForm.this.zoomPercent);
				}
			});
		}
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
								int viewportWidth = getViewportWidth(), viewportHeight = getViewportHeight();
								g.drawImage(
										image,
										viewportLocX, viewportLocY,
										viewportWidth, viewportHeight,
										iob
								);

								g.setColor(colorCommon);
								g.drawString(urlString, 0, size);
								g.drawString(imageWidth + " × " + imageHeight, 0, size * 2 + 4);
								g.drawString(viewportPercent.toPlainString(), 0, size * 3 + 8);
								g.drawString("vp: " + getViewportWidth() + " - " + getViewportHeight(), 0, size * 4 + 12);
								g.drawString("frame: " + frame.getWidth() + " - " + frame.getHeight(), 0, size * 5 + 16);
								g.drawString("mode: " + zoomMode, 0, size * 6 + 20);
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
		void adjustImageZoom(ConfigZoomMode mode)
		{
			synchronized (LOCK)
			{
				PiviImageForm.this.adjustImageZoom(mode, null);
			}
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
				final int viewportWidthOld = getViewportWidth(), viewportHeightOld = getViewportHeight();
				PiviImageForm.this.zoomMode = ConfigZoomMode.CustomPercent;
				PiviImageForm.this.isViewportDragged = true;

				if(movement < 0) // up
				{
					PiviImageForm.this.viewportPercent =
							new BigDecimal(
									PiviImageForm.this.viewportPercent.add(
											new BigDecimal(Pivi.config.getZoomSpeed())
									).longValue()
							);

				}
				else if(movement > 0) // down
				{
					PiviImageForm.this.viewportPercent =
							PiviImageForm.this.viewportPercent.subtract(
									new BigDecimal(Pivi.config.getZoomSpeed())
							);

					final int viewportWidthTemp = getViewportWidth(), viewportHeightTemp = getViewportHeight();
					if(viewportWidthTemp < 16 && viewportHeightTemp < 16)
					{
						viewportPercent = imageHeight > imageWidth ?
								B100.multiply(new BigDecimal(16))
										.divide(new BigDecimal(imageWidth), RoundingMode.CEILING) :
								B100.multiply(new BigDecimal(16))
										.divide(new BigDecimal(imageHeight), RoundingMode.CEILING);
					}
				}
				final int viewportWidthNew = getViewportWidth(), viewportHeightNew = getViewportHeight();
				viewportLocX -= (viewportWidthNew - viewportWidthOld) / 2;
				viewportLocY -= (viewportHeightNew - viewportHeightOld) / 2;
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
				isViewportDragged = true;
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
	public class ViewportKeyListener extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			System.out.println("pressed shift:" + (e.getKeyCode() == KeyEvent.VK_SHIFT));
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			System.out.println("released shift:" + (e.getKeyCode() == KeyEvent.VK_SHIFT));
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
						PiviImageForm.this.adjustImageZoom(PiviImageForm.this.zoomMode, PiviImageForm.this.zoomPercent);
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
