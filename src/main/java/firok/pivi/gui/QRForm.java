package firok.pivi.gui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class QRForm
{
	private JPanel pBase;
	private JButton copyAllButton;
	private JButton closeButton;
	private JList list1;

	private void createUIComponents()
	{
		// TODO: place custom component creation code here
		list1 = new JList(){

		};
		list1.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				super.mouseClicked(e);
				e.getClickCount();
			}
		});

	}
}
