package firok.pivi;

import firok.pivi.beacon.BeaconLit;
import firok.pivi.config.ConfigBean;
import firok.pivi.gui.PiviBeaconForm;
import firok.pivi.gui.PiviImageForm;
import firok.pivi.util.*;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

public class Pivi
{
	public static final String name = "Pivi";
	public static final String version = "0.2.x";
	public static final String author = "Firok";
	public static final String github = "https://github.com/351768593/Pivi";

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

	public static JFrame frameBeacon;
	public static List<JFrame> listFrameImage = new Vector<>();

	public static TransferHandler dh = new TransferHandler("jpg"){
		@Override
		public boolean canImport(TransferSupport support)
		{
			System.out.println("can import [support]");
			return true;
		}

		@Override
		public boolean importData(TransferSupport support)
		{
			System.out.println("import data [support]");
			return true;
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			return NONE;
		}


	};

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
			piviImage.initFromConfig(config);

			// 标准操作
			frame.setContentPane(piviImage.pViewport);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			// 根据配置文件修改大小
			frame.setLocation(config.getInitLocX(), config.getInitLocY());
			frame.setSize(config.getInitWidth(), config.getInitHeight());
			frame.setExtendedState(config.getInitFrameState());
			frame.setTitle(raw.length() < 40 ? raw : raw.substring(0, 40) + "...");

			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowOpened(WindowEvent e)
				{
					piviImage.startLoading(raw);
					listFrameImage.add(frame);
				}

				@Override
				public void windowClosing(WindowEvent e)
				{
					piviImage.stopLoading();
					listFrameImage.remove(frame);
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
				JOptionPane.showMessageDialog(null, "Pivi 启动失败, 请检查信标端口号是否被其它程序占用");
			}
		}
	}

	private static void run_version()
	{
		System.out.println(name + " " + version + " by " + author);
		System.out.println(github);
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
}
