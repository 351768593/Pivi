package firok.pivi.gui;

import firok.pivi.Pivi;
import firok.pivi.config.ConfigBean;
import firok.pivi.config.ConfigZoomMode;
import org.pushingpixels.radiance.theming.api.skin.RadianceAutumnLookAndFeel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collections;

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
			inUrl.setText("");
		});

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

	}

	private void changeLAF(String lafClassName)
	{
		Pivi.changeLAF(lafClassName);
		Pivi.config.setLafClassName(lafClassName);
	}

	public void loadConfig(ConfigBean config)
	{
		final var zm = config.getInitZoomMode();
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
