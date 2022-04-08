package firok.pivi.gui;

import firok.pivi.Pivi;
import firok.pivi.config.ConfigBean;
import firok.pivi.config.ConfigZoomMode;
import firok.pivi.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

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
	public ResourceUtil.FileType fileType;
	public byte[] fileData;
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
			(this.fileType != null &&
			this.fileType.isAnimatedImage) ||
			this.needRepainting
		) && ( // 判断窗体状态
			isFrameReady()
		);
	}
	private boolean isFrameReady()
	{
		return this.frame != null && this.frame.isFocused() && this.frame.isVisible();
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
		static final Color colorShiftModeBackground = new Color(0x545a6b);
		static final Color colorShiftModeForeground = new Color(0xB8CAFC);
		static final Font fontShiftModeForeground = new Font("黑体", Font.BOLD, 16);
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

//								g.setColor(colorCommon);
//								g.drawString(urlString, 0, size);
//								g.drawString(imageWidth + " × " + imageHeight, 0, size * 2 + 4);
//								g.drawString(viewportPercent.toPlainString(), 0, size * 3 + 8);
//								g.drawString("vp: " + getViewportWidth() + " - " + getViewportHeight(), 0, size * 4 + 12);
//								g.drawString("frame: " + frame.getWidth() + " - " + frame.getHeight(), 0, size * 5 + 16);
//								g.drawString("mode: " + zoomMode, 0, size * 6 + 20);

								if(isShiftMode) // Shift模式 渲染一个状态标志出来
								{
									g.setFont(fontShiftModeForeground);

									// 左上侧缩放百分比
									var strNumber = viewportPercent.toPlainString();
									int lengthNumber = strNumber.length();
									int pixExtend = (lengthNumber > 2 ? lengthNumber - 2 : 0) * 8;
									g.setColor(colorShiftModeBackground);
									g.fillRect(20, 20, 43 + pixExtend, 22);
									g.setColor(colorShiftModeForeground);
									g.drawString(strNumber, 24, 36);
									g.drawString("%", 47 + pixExtend, 36);

									// 左下侧标志
									g.setColor(colorShiftModeBackground);
									g.fillRect(20, graphicHeight - 20 - 16 - 8, 52, 22);
									g.setColor(colorShiftModeForeground);
									g.drawString("Shift", 26, graphicHeight - 20 - 8);
								}
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

		final JLabel labelReading;
		final JLabel labelNoResult;
//		final JPanel panelListQRCodes;

		java.util.List<com.google.zxing.Result> listResult;
		java.util.List<JComponent> listItemResult;
		FramePopupMenu()
		{
			super();
			miCopyFullURL = new JMenuItem("📋 | 完整 URL");
			miCopyFullURL.addActionListener(e -> ClipboardUtil.putTextIntoClipboard(PiviImageForm.this.urlString));
			add(miCopyFullURL);

			miCopyFullShort = new JMenuItem("📋 | 文件名");
			miCopyFullShort.addActionListener(e -> ClipboardUtil.putTextIntoClipboard(PiviImageForm.this.urlShort));
			add(miCopyFullShort);

			add(new JSeparator());


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

			add(new JSeparator());

			listResult = new java.util.ArrayList<>();
			listItemResult = new java.util.ArrayList<>();

			labelReading = new JLabel("(二维码识别中...)");
			labelReading.setVisible(true);
			add(labelReading);

			labelNoResult = new JLabel("(无二维码)");
			labelNoResult.setVisible(false);
			add(labelNoResult);

//			panelListQRCodes = new JPanel();
//			add(panelListQRCodes);

			add(new JSeparator());

			for(var objMapping : Pivi.qsave.getListMapping())
			{
				var name = objMapping.name;
				var value = objMapping.value;
				var mi = new JMenuItem("📥 | " + ( name.length() > 0 ? name : value ));
				mi.addActionListener(e -> {
					synchronized (PiviImageForm.this.LOCK)
					{
						if(PiviImageForm.this.status == EnumLoadingStatus.Finished)
						{
							// 决定文件名
							var fm = Pivi.config.getFilenameMethod();

							var filename = switch (fm)
							{
								case UseTimestamp -> genTimestampFilename();
								case UseCustom -> genCustomFilename();
							};
							if(filename == null) return; // 出现这种情况是因为用户取消快速保存

							if(!value.exists()) value.mkdirs();
							var file = new File(value, filename);
							System.out.println(file.getAbsoluteFile());
							try
							{
								ResourceUtil.writeBytes(file, fileData);
							}
							catch (Exception ignored) {
								System.out.println(ignored);
							}
						}
					}
				});
				add(mi);
			}

			pack();
		}

		private String genTimestampFilename()
		{
			return getCurrentTimestamp() + "." + PiviImageForm.this.fileType.toString().toLowerCase();
		}
		private String genCustomFilename()
		{
			return JOptionPane.showInputDialog(PiviImageForm.this.frame, "快速保存文件名", genTimestampFilename());
		}

		private static final SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd hhmm24ss");
		public static String getCurrentTimestamp()
		{
			synchronized (formatter)
			{
				return formatter.format(new java.util.Date());
			}
		}

		public void setListResult(java.util.List<com.google.zxing.Result> listResult)
		{
//			this.panelListQRCodes.removeAll();
			this.listItemResult.forEach(FramePopupMenu.this::remove);
			this.listResult.clear();
			this.listItemResult.clear();

			if(listResult != null && !listResult.isEmpty())
			{
				this.listResult.addAll(listResult);
				EventQueue.invokeLater(()-> listResult.forEach(result -> {

					var content = result.getText();
					var contentCut = content.length() >= 20 ?
							content.substring(0, 17) + "..." :
							content;
					var miClipboard = new JMenuItem("📋 | " + contentCut);
					miClipboard.addActionListener(e -> ClipboardUtil.putTextIntoClipboard(content));
					FramePopupMenu.this.add(miClipboard, 8);
					FramePopupMenu.this.listItemResult.add(miClipboard);

					// 尝试识别是否为uri
					try
					{
						var url = new URL(content);
						var uri = url.toURI();
						var miUri = new JMenuItem("🌏 | "+contentCut);
						miUri.addActionListener(e -> BrowserUtil.accessURI(uri));
						FramePopupMenu.this.add(miUri, 8);
						FramePopupMenu.this.listItemResult.add(miUri);
					}
					catch (MalformedURLException | URISyntaxException ignored) { }
				}));
			}

			boolean hasQRCodes = !this.listResult.isEmpty();
			FramePopupMenu.this.labelNoResult.setVisible(!hasQRCodes);
			FramePopupMenu.this.labelReading.setVisible(false);
			FramePopupMenu.this.pack();
			FramePopupMenu.this.repaint();
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
				if(!isFrameReady()) return;

				final int movement = e.getWheelRotation();
				needRepainting = true;
				final int viewportWidthOld = getViewportWidth(), viewportHeightOld = getViewportHeight();
				PiviImageForm.this.zoomMode = ConfigZoomMode.CustomPercent;
				PiviImageForm.this.isViewportDragged = true;

				if(isShiftMode)
				{
					PiviImageForm.this.viewportPercent =
							movement < 0 ? PiviImageForm.this.viewportPercent.add(BigDecimal.ONE) :
									PiviImageForm.this.viewportPercent.subtract(BigDecimal.ONE);
				}
				else
				{
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
			synchronized (PiviImageForm.this.LOCK)
			{
				PiviImageForm.this.isShiftMode = true;
				PiviImageForm.this.needRepainting = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			synchronized (PiviImageForm.this.LOCK)
			{
				PiviImageForm.this.isShiftMode = false;
				PiviImageForm.this.needRepainting = true;
			}
		}
	}

	private Thread threadLoad;
	private Thread threadAnimation;
	public void startLoading(String raw)
	{
		stopLoading();
		threadLoad = new ThreadLoadURI(raw);
		threadLoad.start();
	}
	public void startLoading(Image image)
	{
		stopLoading();
		threadLoad = new ThreadLoadImage(image);
		threadLoad.start();
	}
	public void stopLoading()
	{
		if(threadLoad != null)
		{
			threadLoad.interrupt();
			threadLoad = null;
		}
	}

	protected class ThreadLoader extends Thread
	{
		protected ThreadLoader()
		{
			super();
		}
		protected void startLoading(URL _u)
		{
			PiviImageForm.this.url = _u;
			var _us = _u.toString();
			PiviImageForm.this.urlString = _us;
			var _ius = -1;
			_ius = (_ius = _us.lastIndexOf('/')) > 0 ? _ius : _us.lastIndexOf('\\');
			PiviImageForm.this.urlShort = _ius > 0 ? _us.substring(_ius + 1) : _us;
			PiviImageForm.this.status = EnumLoadingStatus.Started;
		}
		protected void finishLoad(ImageIcon _ii, Image _i, ResourceUtil.FileType fileType, byte[] fileData)
		{
			PiviImageForm.this.status = EnumLoadingStatus.Finished;
			PiviImageForm.this.image = _i;
			PiviImageForm.this.fileType = fileType;
			PiviImageForm.this.fileData = fileData;
			PiviImageForm.this.iob = _ii.getImageObserver();
			PiviImageForm.this.imageWidth = _i.getWidth(PiviImageForm.this.iob);
			PiviImageForm.this.imageHeight = _i.getHeight(PiviImageForm.this.iob);
			PiviImageForm.this.adjustImageZoom(PiviImageForm.this.zoomMode, PiviImageForm.this.zoomPercent);
		}
		protected void errorURL(String raw)
		{
			PiviImageForm.this.url = null;
			PiviImageForm.this.status = EnumLoadingStatus.Error;
			PiviImageForm.this.exception = new Exception("无法识别路径: "+raw);
			PiviImageForm.this.image = null;
		}
		protected void errorReadImage(Exception e)
		{
			PiviImageForm.this.status = EnumLoadingStatus.Error;
			PiviImageForm.this.exception = e;
		}
		protected void scanQRCodes(Image _i)
		{
			var image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			var g = image.createGraphics();
			g.drawImage(_i, 0, 0, imageWidth, imageHeight, (img, infoflags, x, y, width, height) -> false);
			g.dispose();
			var listResult = QRUtil.scanMultiQRCode(image);
			var menu = (FramePopupMenu) PiviImageForm.this.menu;
			menu.setListResult(listResult);
		}
	}
	protected class ThreadLoadURI extends ThreadLoader
	{
		String raw;
		ThreadLoadURI(String raw)
		{
			super();
			setDaemon(true);
			this.raw = raw;
		}

		@Override
		public void run()
		{
			PiviImageForm.this.status = EnumLoadingStatus.NotStarted;
			ImageIcon _ii = null;
			Image _i = null;
			boolean shouldScanQR = false;
			try
			{
				var _u = ResourceUtil.readUrl(raw);
				if(_u == null)
				{
					synchronized (LOCK)
					{
						errorURL(raw);
					}
				}
				else
				{
					synchronized (LOCK)
					{
						startLoading(_u);
					}

					var bytes = ResourceUtil.readBytes(_u);
					var fileHeader = ResourceUtil.getDataHeader(bytes);
					var fileType = ResourceUtil.getFileTypeFromDataHeader(fileHeader);
					_ii = new ImageIcon(bytes);
					_i = _ii.getImage();
					if(_i == null)
					{
						throw new RuntimeException("无法读取为图片: "+_u);
					}

					frame.setIconImage(_i);

					synchronized (LOCK)
					{
						finishLoad(_ii, _i, fileType, bytes);

						shouldScanQR = PiviImageForm.this.status == EnumLoadingStatus.Finished &&
								!PiviImageForm.this.fileType.isAnimatedImage &&
								PiviImageForm.this.imageWidth > 0 &&
								PiviImageForm.this.imageHeight > 0;
					}

					PiviImageForm.this.pViewport.repaint();
				}
			}
			catch (Exception e)
			{
				synchronized (LOCK)
				{
					errorReadImage(e);
				}
			}

			// 开始识别二维码
			if(shouldScanQR)
			{
				scanQRCodes(_i);
			}
		}
	}
	protected class ThreadLoadImage extends ThreadLoader
	{
		Image image;
		ThreadLoadImage(Image image)
		{
			super();
			setDaemon(true);
			this.image = image;
		}

		@Override
		public void run()
		{
			PiviImageForm.this.status = EnumLoadingStatus.NotStarted;

		}
	}
}
