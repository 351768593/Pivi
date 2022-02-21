package firok.pivi;

import firok.pivi.config.ConfigBean;
import firok.pivi.gui.PiviForm;
import firok.pivi.scene.SceneManager;
import firok.pivi.util.TimeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;

public class Pivi
{
	public static final String name = "Pivi";
	public static final String version = "0.1.0";
	public static final String author = "Firok";

	public static final File fileConfig = new File("./pivi.conf");
	public static ConfigBean config;
	private static void initConfig()
	{
		config = new ConfigBean();
		try
		{
			var configFile = ConfigBean.fromFile(fileConfig);
			config.override(configFile);
		}
		catch (Exception ignored) { }
	}

	private static void initLAF()
	{
		try
		{
			UIManager.setLookAndFeel(config.getLafClassName());
			UIManager.put("defaultFont", new Font("Microsoft Yahei", Font.PLAIN, 12));
		}
		catch (Exception ignored) {
			try
			{
				var laf = new com.formdev.flatlaf.FlatDarkLaf();
				UIManager.setLookAndFeel(laf);
			}
			catch (Exception ignored2) { }
		}
	}

	public static JFrame frame;
	public static PiviForm piviInstance;
	private static void initFrame()
	{
		piviInstance = new PiviForm();
		frame = new JFrame(name + " " + version);
		int initLocX = config.getInitLocX(), initLocY = config.getInitLocY();
		int initWidth = config.getInitWidth(), initHeight = config.getInitHeight();
		int initFrameState = config.getInitFrameState();

		var minSize = new Dimension(366, 366);
		frame.setMinimumSize(minSize);
		frame.setLocation(initLocX, initLocY);
		frame.setSize(initWidth, initHeight);
		frame.setExtendedState(initFrameState);

		frame.setContentPane(piviInstance.pForm);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.pack();
	}

	public static SceneManager smgr;
	private static void initScene()
	{
		smgr = new SceneManager();
	}

	private static void postInitFrame()
	{
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				try
				{
					var configOld = ConfigBean.fromFile(fileConfig);

					var configBase = configOld.getTimestamp() > config.getTimestamp() ? configOld : config;

					configBase.setInitFrameState(frame.getExtendedState());
					var pt = frame.getLocationOnScreen();
					configBase.setInitLocX(pt.x);
					configBase.setInitLocY(pt.y);
					configBase.setInitWidth(frame.getWidth());
					configBase.setInitHeight(frame.getHeight());

					configBase.toFile(fileConfig);
				}
				catch (Exception ignored) { }
			}
		});
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println("program args, "+args.length+":");
		System.out.println(Arrays.toString(args));
		System.out.println("====");

		initConfig();
		initLAF();
		initScene();
		initFrame();
		frame.setVisible(true);
		postInitFrame();
		smgr.startThread();
	}
}
