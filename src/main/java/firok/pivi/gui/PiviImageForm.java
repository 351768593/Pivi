package firok.pivi.gui;

import firok.pivi.config.ConfigBean;

import javax.swing.*;

public class PiviImageForm
{
	public JFrame frame;
	public JPanel pViewport;
	public JPopupMenu menu;

	public void initFromConfig(ConfigBean config)
	{
		;
	}

	private void createUIComponents()
	{
		pViewport = new JPanel(){};

		menu = new JPopupMenu("测试menu");
		menu.add("123");
		menu.add("456");

		pViewport.setComponentPopupMenu(menu);
	}

	private Thread threadLoad;
	public void startLoading(String raw)
	{
		threadLoad = new Thread();
		threadLoad.start();
	}
	public void stopLoading()
	{
		if(threadLoad != null)
			threadLoad.interrupt();
	}
}
