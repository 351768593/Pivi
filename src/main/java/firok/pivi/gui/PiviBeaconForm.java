package firok.pivi.gui;

import firok.pivi.Pivi;
import firok.pivi.config.ConfigBean;
import firok.pivi.config.ConfigZoomMode;
import firok.pivi.util.BrowserUtil;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.util.Arrays;

public class PiviBeaconForm
{
	public JTabbedPane tpBase;
	public JPanel pBase;
	public JTextField inUrl;
	public JButton btnLoad;
	public JRadioButton rbFitWidth;
	public JRadioButton rbFitHeight;
	public JRadioButton rbCustomPercent;
	public JSpinner inCustomPercent;
	public JRadioButton rbOriginSize;
	public JSpinner inZoomSpped;
	public JSpinner inBeaconPort;
	private JComboBox<EnumComboLAF> cbxLAF;
	private JRadioButton rbFitAuto;
	private JButton btnSelect;
	private JButton viewOnGitHubButton;
	private JLabel labelPivi;
	private JLabel labelVersion;
	private JLabel labelDescription;

	private void createUIComponents()
	{
		labelPivi = new JLabel("Pivi", JLabel.CENTER);
		labelPivi.setFont(new Font("Ink Free", Font.BOLD, 48));

		labelVersion = new JLabel(Pivi.version + "  by " + Pivi.author, JLabel.CENTER);
		labelVersion.setFont(new Font("Ink Free", Font.PLAIN, 16));

		labelDescription = new JLabel(Pivi.description);
		labelDescription.setFont(new Font("Ink Free", Font.PLAIN, 22));
	}

	private enum EnumComboLAF
	{
		FlatDark("Flat Dark", "com.formdev.flatlaf.FlatDarkLaf"),
		FlatLight("Flat Light", "com.formdev.flatlaf.FlatLightLaf"),
		Metal("Metal", "javax.swing.plaf.metal.MetalLookAndFeel"),
		Nimbus("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel"),
		CDE("CED / Modif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
		Windows("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
		WindowsClassic("Windows Classic", "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"),
		RadianceAutumnLookAndFeel("Radiance Autumn", "org.pushingpixels.radiance.theming.api.skin.RadianceAutumnLookAndFeel"),
		RadianceBusinessBlackSteelLookAndFeel("Radiance Business Black Steel", "org.pushingpixels.radiance.theming.api.skin.RadianceBusinessBlackSteelLookAndFeel"),
		RadianceBusinessBlueSteelLookAndFeel("Radiance Business Blue Steel", "org.pushingpixels.radiance.theming.api.skin.RadianceBusinessBlueSteelLookAndFeel"),
		RadianceBusinessLookAndFeel("Radiance Business", "org.pushingpixels.radiance.theming.api.skin.RadianceBusinessLookAndFeel"),
		RadianceCeruleanLookAndFeel("Radiance Cerulean", "org.pushingpixels.radiance.theming.api.skin.RadianceCeruleanLookAndFeel"),
		RadianceCremeCoffeeLookAndFeel("Radiance Creme Coffee", "org.pushingpixels.radiance.theming.api.skin.RadianceCremeCoffeeLookAndFeel"),
		RadianceCremeLookAndFeel("Radiance Creme", "org.pushingpixels.radiance.theming.api.skin.RadianceCremeLookAndFeel"),
		RadianceDustCoffeeLookAndFeel("Radiance Dust Coffee", "org.pushingpixels.radiance.theming.api.skin.RadianceDustCoffeeLookAndFeel"),
		RadianceDustLookAndFeel("Radiance Dust", "org.pushingpixels.radiance.theming.api.skin.RadianceDustLookAndFeel"),
		RadianceGeminiLookAndFeel("Radiance Gemini", "org.pushingpixels.radiance.theming.api.skin.RadianceGeminiLookAndFeel"),
		RadianceGraphiteAquaLookAndFeel("Radiance Graphite Aqua", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteAquaLookAndFeel"),
		RadianceGraphiteChalkLookAndFeel("Radiance Graphite Chalk", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteChalkLookAndFeel"),
		RadianceGraphiteElectricLookAndFeel("Radiance Graphite Electric", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteElectricLookAndFeel"),
		RadianceGraphiteGlassLookAndFeel("Radiance Graphite Glass", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteGlassLookAndFeel"),
		RadianceGraphiteGoldLookAndFeel("Radiance Graphite Gold", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteGoldLookAndFeel"),
		RadianceGraphiteLookAndFeel("Radiance Graphite", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteLookAndFeel"),
		RadianceGraphiteSiennaLookAndFeel("Radiance Graphite Sienna", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteSiennaLookAndFeel"),
		RadianceGraphiteSunsetLookAndFeel("Radiance Graphite Sunset", "org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteSunsetLookAndFeel"),
		RadianceGreenMagicLookAndFeel("Radiance Green Magic", "org.pushingpixels.radiance.theming.api.skin.RadianceGreenMagicLookAndFeel"),
		RadianceMagellanLookAndFeel("Radiance Magellan", "org.pushingpixels.radiance.theming.api.skin.RadianceMagellanLookAndFeel"),
		RadianceMarinerLookAndFeel("Radiance Mariner", "org.pushingpixels.radiance.theming.api.skin.RadianceMarinerLookAndFeel"),
		RadianceMistAquaLookAndFeel("Radiance Mist Aqua", "org.pushingpixels.radiance.theming.api.skin.RadianceMistAquaLookAndFeel"),
		RadianceMistSilverLookAndFeel("Radiance Mist Silver", "org.pushingpixels.radiance.theming.api.skin.RadianceMistSilverLookAndFeel"),
		RadianceModerateLookAndFeel("Radiance Moderate", "org.pushingpixels.radiance.theming.api.skin.RadianceModerateLookAndFeel"),
		RadianceNebulaAmethystLookAndFeel("Radiance Nebula Amethyst", "org.pushingpixels.radiance.theming.api.skin.RadianceNebulaAmethystLookAndFeel"),
		RadianceNebulaBrickWallLookAndFeel("Radiance Nebula Brick Wall", "org.pushingpixels.radiance.theming.api.skin.RadianceNebulaBrickWallLookAndFeel"),
		RadianceNebulaLookAndFeel("Radiance Nebula", "org.pushingpixels.radiance.theming.api.skin.RadianceNebulaLookAndFeel"),
		RadianceNightShadeLookAndFeel("Radiance Night Shade", "org.pushingpixels.radiance.theming.api.skin.RadianceNightShadeLookAndFeel"),
		RadianceRavenLookAndFeel("Radiance Raven", "org.pushingpixels.radiance.theming.api.skin.RadianceRavenLookAndFeel"),
		RadianceSaharaLookAndFeel("Radiance Sahara", "org.pushingpixels.radiance.theming.api.skin.RadianceSaharaLookAndFeel"),
		RadianceSentinelLookAndFeel("Radiance Sentinel", "org.pushingpixels.radiance.theming.api.skin.RadianceSentinelLookAndFeel"),
		RadianceTwilightLookAndFeel("Radiance Twilight", "org.pushingpixels.radiance.theming.api.skin.RadianceTwilightLookAndFeel"),
		;
		public final String lafName, lafClassName;
		EnumComboLAF(String lafName, String lafClassName)
		{
			this.lafName = lafName;
			this.lafClassName = lafClassName;
		}

		@Override
		public String toString()
		{
			return lafName;
		}
	}

	private File filePreviousFolder = new File("./");
	public PiviBeaconForm()
	{
		btnLoad.addActionListener(e ->
		{
			var raw = inUrl.getText();
			if(raw != null)
			{
				raw = raw.trim();
				if(raw.length() > 0)
				{
					if(raw.startsWith("\"") && raw.endsWith("\""))
						raw = raw.substring(1, raw.length() - 1);
					Pivi.initFrameImage(raw);
				}
			}
//			inUrl.setText("");
		});
		btnSelect.addActionListener(e ->
		{
			var dialog = new JFileChooser(filePreviousFolder);
			dialog.setDragEnabled(false);
			dialog.setFileFilter(new FileFilter()
			{
				static final String[] suffixes = new String[]{
						".jpg", ".jpeg", ".gif", ".png",
				};
				static final String suffixJoint = String.join(", ", suffixes);

				@Override
				public boolean accept(File f)
				{
					var name = f.getName();
					for(var suffix : suffixes)
					{
						if(name.endsWith(suffix)) return true;
					}
					return false;
				}

				@Override
				public String getDescription()
				{
					return suffixJoint;
				}
			});
			dialog.setAcceptAllFileFilterUsed(true);
			dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			dialog.setMultiSelectionEnabled(true);

			var result = dialog.showOpenDialog(PiviBeaconForm.this.pBase);
			if(result != JFileChooser.APPROVE_OPTION) return;

			var arrFileSelected = dialog.getSelectedFiles();
			if(arrFileSelected.length > 0)
			{
				filePreviousFolder = arrFileSelected[0].getParentFile();
			}
			for(var objFileSelected : arrFileSelected)
			{
				var pathFileSelected = objFileSelected.getAbsolutePath();
				Pivi.initFrameImage(pathFileSelected);
			}
		});

		rbFitAuto.addActionListener(e -> Pivi.config.setInitZoomMode(ConfigZoomMode.FitAuto));
		rbFitWidth.addActionListener(e -> Pivi.config.setInitZoomMode(ConfigZoomMode.FitWindowWidth));
		rbFitHeight.addActionListener(e -> Pivi.config.setInitZoomMode(ConfigZoomMode.FitWindowHeight));
		rbOriginSize.addActionListener(e -> Pivi.config.setInitZoomMode(ConfigZoomMode.OriginSize));
		rbCustomPercent.addActionListener(e -> Pivi.config.setInitZoomMode(ConfigZoomMode.CustomPercent));
		inCustomPercent.setModel(new SpinnerNumberModel((int) Pivi.config.getInitZoomPercent(), 1, 10000, 1));
		inCustomPercent.addChangeListener(e -> Pivi.config.setInitZoomPercent((Integer) inCustomPercent.getValue()));
		inZoomSpped.setModel(new SpinnerNumberModel((int) Pivi.config.getZoomSpeed(), 1, 1000, 1));
		inZoomSpped.addChangeListener(e -> Pivi.config.setZoomSpeed((Integer) inZoomSpped.getValue()));
		inBeaconPort.setModel(new SpinnerNumberModel((int) Pivi.config.getBeaconPort(), 1, 65535, 1));
		inBeaconPort.addChangeListener(e -> Pivi.config.setBeaconPort((Integer) inBeaconPort.getValue()));
		var model = new DefaultComboBoxModel<EnumComboLAF>();
		model.addAll(Arrays.asList(EnumComboLAF.values()));
		cbxLAF.setModel(model);
		cbxLAF.addItemListener(e ->
		{
			if(e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof EnumComboLAF eclaf)
			{
				changeLAF(eclaf.lafClassName);
			}
		});

		viewOnGitHubButton.addActionListener(e -> BrowserUtil.accessURI(Pivi.url));
	}

	private void changeLAF(String lafClassName)
	{
		try
		{
			Pivi.changeLAF(lafClassName);
			Pivi.config.setLafClassName(lafClassName);
		}
		// fixme low 这个地方的错误很怪
		//   暂时不确定原因是什么
		//   以后有空研究一下
		//   目前怀疑是初始化主题的错误导致的
		catch (Exception e)
		{

			JOptionPane.showMessageDialog(
					null,
					"重启 Pivi 以完成此主题切换"
			);
			Pivi.config.setLafClassName(lafClassName);
			Pivi.extinguish();
		}
	}

	public void loadConfig(ConfigBean config)
	{
		final var zm = config.getInitZoomMode();
		rbFitAuto.setSelected(zm == ConfigZoomMode.FitAuto);
		rbFitWidth.setSelected(zm == ConfigZoomMode.FitWindowWidth);
		rbFitHeight.setSelected(zm == ConfigZoomMode.FitWindowHeight);
		rbOriginSize.setSelected(zm == ConfigZoomMode.OriginSize);
		rbCustomPercent.setSelected(zm == ConfigZoomMode.CustomPercent);
		final var lafClassName = config.getLafClassName();
		for(var eclaf : EnumComboLAF.values())
		{
			if(eclaf.lafClassName.equals(lafClassName))
			{
				cbxLAF.setSelectedItem(eclaf);
				break;
			}
		}
		inZoomSpped.setValue(config.getZoomSpeed());
		inBeaconPort.setValue(config.getBeaconPort());
		inCustomPercent.setValue(config.getInitZoomPercent());
	}
}
