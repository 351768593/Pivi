package firok.pivi;

import firok.pivi.beacon.BeaconLit;
import firok.pivi.config.ConfigBean;
import firok.pivi.gui.PiviBeaconForm;
import firok.pivi.gui.PiviImageForm;
import firok.pivi.util.*;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class Pivi
{
	public static String name;
	public static String version;
	public static String description;
	public static String author;
	public static String url;
	public static Image imageIcon;
	static
	{
		try(var ios = ClassLoader.getSystemResourceAsStream("project.properties"))
		{
			Properties props = new Properties();
			props.load(ios);
			name = props.getProperty("project.name");
			version = props.getProperty("project.version");
			description = props.getProperty("project.description");
			author = props.getProperty("project.author");
			url = props.getProperty("project.url");
		}
		catch (Exception e)
		{
			name = "Pivi";
			version = "0.2.x";
			description = "A simple picture viewer.";
			author = "Firok";
			url = "https://github.com/351768593/Pivi";
		}

		try(var ios = ClassLoader.getSystemResourceAsStream("icon.png"))
		{
			assert ios != null;
			var bytes = ios.readAllBytes();
			imageIcon = new ImageIcon(bytes).getImage();
		}
		catch (Exception ignored) {
			System.err.println("读取图片错误");
			System.err.println(ignored);
		}
	}

	public static final File fileConfig = new File("./pivi.conf");

	private static void initLAF(String lafClassName)
	{
		try
		{
			UIManager.setLookAndFeel(lafClassName);
			UIManager.put("defaultFont", new Font("Microsoft Yahei", Font.PLAIN, 12));
		}
		catch (Exception ignored)
		{
			try
			{
				UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
				UIManager.put("defaultFont", new Font("Microsoft Yahei", Font.PLAIN, 12));
			}
			catch (Exception ignored2) { }
		}
	}
	public static void changeLAF(String lafClassName)
	{
		initLAF(lafClassName);
		if(frameBeacon != null)
			SwingUtilities.updateComponentTreeUI(frameBeacon);
		listFrameImage.forEach(SwingUtilities::updateComponentTreeUI);
	}

	public static ConfigBean config;
	public static void initConfig()
	{
		config = new ConfigBean();
		try
		{
			var configFile = ConfigBean.fromFile(fileConfig);
			config.override(configFile);
		}
		catch (Exception ignored) { }
	}
	public static void saveConfig()
	{
		try
		{
			config.toFile(fileConfig);
		}
		catch (Exception ignored) { }
	}

	public final Object LOCK = new Object();
	public static JFrame frameBeacon;
	public static List<JFrame> listFrameImage = new Vector<>();
	public static List<PiviImageForm> listPiviImage = new Vector<>();
	public static ThreadAnimation threadAnimation;

	@SneakyThrows
	public static void initFrameBeacon()
	{
		EventQueue.invokeLater(()->{
			JFrame frame = new JFrame();
			var piviBeacon = new PiviBeaconForm();

			// 标准操作
			frame.setContentPane(piviBeacon.pBase);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			// 初始大小
			var size00 = new Dimension(0, 0);
			frame.setMaximumSize(size00);
			frame.setSize(size00);
			if(imageIcon != null) frame.setIconImage(imageIcon);

			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowOpened(WindowEvent e)
				{

					frameBeacon = frame;
					piviBeacon.loadConfig(config);
					changeLAF(config.getLafClassName());

					frame.setMinimumSize(new Dimension(600, 400));
					frame.setSize(600, 400);
					frame.setLocationRelativeTo(null); // 居中显示
					frame.setTitle(name);
				}

				@Override
				public void windowClosing(WindowEvent e)
				{
					saveConfig();
					frameBeacon = null;
				}
			});

			frame.setVisible(true);
		});
	}
	@SneakyThrows
	public static void initFrameImage(String raw)
	{
		EventQueue.invokeLater(()->{
			JFrame frame = new JFrame();
			var piviImage = new PiviImageForm();
			piviImage.postInit(config, frame);

			// 标准操作
			frame.setContentPane(piviImage.pViewport);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			// 根据配置文件修改大小
			frame.setLocation(config.getInitLocX(), config.getInitLocY());
			frame.setSize(config.getInitWidth(), config.getInitHeight());
			frame.setExtendedState(config.getInitFrameState());
			frame.setTitle(raw.length() < 40 ? raw : raw.substring(0, 40) + "...");
			if(imageIcon != null) frame.setIconImage(imageIcon);

			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowOpened(WindowEvent e)
				{
					piviImage.startLoading(raw);
					listFrameImage.add(frame);
					listPiviImage.add(piviImage);
				}

				@Override
				public void windowClosing(WindowEvent e)
				{
					piviImage.stopLoading();
					listFrameImage.remove(frame);
					listPiviImage.remove(piviImage);
				}
			});

			frame.setVisible(true);
		});
	}

	public static void main(String[] args)
	{
		var ac = new ArgumentCompact(args);
		if(ac.modeHelp) run_help();
		else if(ac.modeVersion) run_version();
		else if(ac.modeGenWin) run_gen_win();
		else // 启动本体
		{
			initConfig();
			if(ac.port != null)
			{
				config.setBeaconPort(ac.port);
			}

			Boolean runResult = null;
			if(BeaconLit.litServer(config.getBeaconPort())) // 尝试启动服务端 如果能启动就直接继续
			{
				System.out.println("Beacon on port: "+config.getBeaconPort());
				initFrameBeacon();
				for(var raw : ac.listPotentialUrl)
				{
					initFrameImage(raw);
				}
				runResult = true;
				threadAnimation = new ThreadAnimation();
				threadAnimation.start();
			}
			else // 服务端无法启动 尝试启动客户端并发送请求
			{
				for(var raw : ac.listPotentialUrl)
				{
					if(BeaconLit.litClient(config.getBeaconPort(), raw))
						runResult = false;
				}
			}

			if(runResult == null)
			{
				JOptionPane.showMessageDialog(
						null,
						"Pivi 启动失败, 请检查信标端口 %d 是否被其它程序占用"
								.formatted(ac.port)
				);
			}
		}
	}

	private static void run_version()
	{
		System.out.println(name + " " + version + " by " + author);
		System.out.println(url);
	}
	private static void run_help()
	{
		System.out.println("""
""");
	}
	private static void run_gen_win()
	{
		;
	}

	// todo 这个地方的协程写法不知道会不会有什么问题 以后可能需要留意一下
	private static class ThreadAnimation extends Thread
	{
		public ThreadAnimation()
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
					for(var objFrame : new ArrayList<>(listPiviImage))
					{
						synchronized (objFrame.LOCK)
						{
							if(objFrame.needRepaint())
							{
								objFrame.frame.repaint();
								objFrame.needRepainting = false;
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					break;
				}

				try
				{
					Thread.sleep(25);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			while (true);
		}
	}
}
