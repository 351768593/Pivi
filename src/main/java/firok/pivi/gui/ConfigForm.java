package firok.pivi.gui;

import firok.pivi.config.ConfigBean;
import firok.pivi.config.ConfigZoomMode;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;

public class ConfigForm
{
	public JPanel pConfigBase;
	public JTabbedPane tabbedPane1;
	private JCheckBox cbURL;
	private JCheckBox cbOperationBar;
	private JCheckBox cbImageInfo;
	private JRadioButton rbFitWidth;
	private JRadioButton rbFitHeight;
	private JRadioButton rbOriginSize;
	private JRadioButton rbCustomPercent;
	private JSpinner inCustomPercent;
	private JCheckBox cbLoadingParticle;
	private JCheckBox cbZoomAnimation;
	private JCheckBox cbScrollBar;
	private JButton btnReloadHotkey;
	private JButton btnResetHotkey;
	private JButton btnHotkeyDocument;
	private JTextField instrQsaveFolder;
	private JButton btnCheckQsaveFolder;
	private JRadioButton rbLAFFlatDark;
	private JRadioButton rbLAFFlatLight;
	private JButton btnResetUIConfig;
	private JButton btnLinkGitHub;

	private ButtonGroup groupImageSize;
	private ButtonGroup groupStyle;

	private boolean isChanged = false;
	private void setChanged(Object placeholder)
	{
		this.isChanged = true;
	}
	public boolean hasChanged()
	{
		return this.isChanged;
	}

	public void setConfig(ConfigBean config)
	{
		this.cbURL.setSelected(config.getIsInitLayoutURL());
		this.cbOperationBar.setSelected(config.getIsInitLayoutOperationBar());
		this.cbImageInfo.setSelected(config.getIsInitLayoutImageInfo());
		this.cbScrollBar.setSelected(config.getIsInitLayoutScrollBar());
		ConfigZoomMode modeZoom = config.getInitZoomMode();
		this.rbFitWidth.setSelected(modeZoom == ConfigZoomMode.FitWindowWidth);
		this.rbFitHeight.setSelected(modeZoom == ConfigZoomMode.FitWindowHeight);
		this.rbOriginSize.setSelected(modeZoom == ConfigZoomMode.OriginSize);
		this.rbCustomPercent.setSelected(modeZoom == ConfigZoomMode.CustomPercent);
		this.inCustomPercent.setValue(config.getInitZoomPercent());
		this.cbLoadingParticle.setSelected(config.getIsAnimationLoadingParticle());
		this.cbZoomAnimation.setSelected(config.getIsAnimationZoom());
		String lafClassName = config.getLafClassName();
		this.rbLAFFlatDark.setSelected(com.formdev.flatlaf.FlatDarkLaf.class.getName().equals(lafClassName));
		this.rbLAFFlatLight.setSelected(com.formdev.flatlaf.FlatLightLaf.class.getName().equals(lafClassName));
	}
	public ConfigBean getConfig()
	{
		var ret = new ConfigBean();
		ret.setIsInitLayoutURL(cbURL.isSelected());
		ret.setIsInitLayoutOperationBar(cbOperationBar.isSelected());
		ret.setIsInitLayoutImageInfo(cbImageInfo.isSelected());
		ret.setIsInitLayoutScrollBar(cbScrollBar.isSelected());
		ConfigZoomMode modeZoom = ConfigZoomMode.OriginSize;
		if(rbFitWidth.isSelected()) modeZoom = ConfigZoomMode.FitWindowWidth;
		if(rbFitHeight.isSelected()) modeZoom = ConfigZoomMode.FitWindowHeight;
		if(rbOriginSize.isSelected()) modeZoom = ConfigZoomMode.OriginSize;
		if(rbCustomPercent.isSelected()) modeZoom = ConfigZoomMode.CustomPercent;
		ret.setInitZoomPercent(((Number) inCustomPercent.getValue()).intValue());
		ret.setInitZoomMode(modeZoom);
		ret.setIsAnimationLoadingParticle(cbLoadingParticle.isSelected());
		ret.setIsAnimationZoom(cbZoomAnimation.isSelected());
		String lafClassName = com.formdev.flatlaf.FlatDarkLaf.class.getName();
		if(rbLAFFlatDark.isSelected()) lafClassName = com.formdev.flatlaf.FlatDarkLaf.class.getName();
		if(rbLAFFlatLight.isSelected()) lafClassName = com.formdev.flatlaf.FlatLightLaf.class.getName();
		ret.setLafClassName(lafClassName);
		return ret;
	}

	public ConfigForm()
	{
		btnResetUIConfig.addActionListener(e -> this.setConfig(new ConfigBean()));
		// 监听值是否发生变化
		cbURL.addActionListener(this::setChanged);
		cbOperationBar.addActionListener(this::setChanged);
		cbScrollBar.addActionListener(this::setChanged);
		cbImageInfo.addActionListener(this::setChanged);
		cbLoadingParticle.addActionListener(this::setChanged);
		cbZoomAnimation.addActionListener(this::setChanged);
		rbFitWidth.addActionListener(this::setChanged);
		rbFitHeight.addActionListener(this::setChanged);
		rbCustomPercent.addActionListener(this::setChanged);
		rbOriginSize.addActionListener(this::setChanged);
		rbLAFFlatDark.addActionListener(this::setChanged);
		rbLAFFlatLight.addActionListener(this::setChanged);
		inCustomPercent.addChangeListener(this::setChanged);
		btnResetUIConfig.addActionListener(this::setChanged);
	}


	private void createUIComponents()
	{
		rbFitWidth = new JRadioButton();
		rbFitHeight = new JRadioButton();
		rbCustomPercent = new JRadioButton();
		rbOriginSize = new JRadioButton();

		groupImageSize = new ButtonGroup();
		groupImageSize.add(rbFitWidth);
		groupImageSize.add(rbFitHeight);
		groupImageSize.add(rbCustomPercent);
		groupImageSize.add(rbOriginSize);

		rbLAFFlatDark = new JRadioButton();
		rbLAFFlatLight = new JRadioButton();

		groupStyle = new ButtonGroup();
		groupStyle.add(rbLAFFlatDark);
		groupStyle.add(rbLAFFlatLight);
	}
}
