package firok.pivi;

import firok.pivi.beacon.BeaconLit;
import firok.pivi.config.ConfigBean;
import firok.pivi.gui.PiviBeaconForm;
import firok.pivi.gui.PiviImageForm;
import firok.pivi.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Vector;

public class Pivi
{
	public static final String name = "Pivi";
	public static final String version = "0.1.1";
	public static final String author = "Firok";

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

	public static void initFrameBeacon()
	{
		JFrame frame = new JFrame();
		var piviBeacon = new PiviBeaconForm();

		// 标准操作
		frame.setContentPane(piviBeacon.pBase);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// 初始大小
		frame.setMinimumSize(new Dimension(600, 400));
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null); // 居中显示

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				frameBeacon = frame;
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				saveConfig();
				frameBeacon = null;
			}
		});

		frame.setVisible(true);
	}
	public static void initFrameImage(String raw)
	{
		JFrame frame = new JFrame();
		var piviImage = new PiviImageForm();

		// 标准操作
		frame.setContentPane(piviImage.pViewport);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// 根据配置文件修改大小
		frame.setLocation(config.getInitLocX(), config.getInitLocY());
		frame.setSize(config.getInitWidth(), config.getInitHeight());
		frame.setExtendedState(config.getInitFrameState());

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e)
			{
				listFrameImage.add(frame);
			}

			@Override
			public void windowClosing(WindowEvent e)
			{
				listFrameImage.remove(frame);
			}
		});
	}

	public static void main(String[] args)
	{
		var ac = new ArgumentCompact(args);
		if(ac.modeHelp) TaskHelp.run();
		else if(ac.modeVersion) TaskVersion.run();
		else if(ac.modeGenWin) TaskGenWin.run();
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
}
