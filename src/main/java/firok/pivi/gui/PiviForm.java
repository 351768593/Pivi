package firok.pivi.gui;

import firok.pivi.Pivi;
import firok.pivi.scene.PanelScene;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class PiviForm
{
	public JPanel pForm;
	public JButton btnQR;
	public JButton btnQuickSave;
	public JButton btnZoomIn;
	public JButton btnZoomOut;
	public JButton btnOriginSize;
	public JButton btnFitSize;
	public JButton btnCopyPath;
	public JButton btnCopyImage;
	public JTextField textField1;
	public JButton btnReload;
	public JButton btnSetting;
	public JPanel pImageViewBase;
	public JScrollBar sbVertical;
	public JScrollBar sbHorizontal;
	public JPanel pScene;
	public JPanel pImageView;

	public PiviForm()
	{
		btnSetting.addActionListener(e ->
		{
			var dia = new JDialog(Pivi.frame, true);

			var formConfig = new ConfigForm();
			formConfig.setConfig(Pivi.config);
			dia.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					Pivi.config = formConfig.getConfig();
				}
			});
			dia.setContentPane(formConfig.pConfigBase);
			dia.pack();
			dia.setLocationRelativeTo(Pivi.frame);
			dia.setSize(400, 350);
			dia.setVisible(true);
		});

	}

	private void createUIComponents()
	{
		// TODO: place custom component creation code here
		pScene = new PanelScene(Pivi.smgr);
		pScene.setTransferHandler(new TransferHandler(){
			public boolean importData(JComponent comp, Transferable t) {
				try
				{
					var objTransferData = t.getTransferData(DataFlavor.javaFileListFlavor);
					System.out.println(objTransferData);
					return true;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				return false;
			}

			@Override
			public Image getDragImage()
			{
				return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			}

			@Override
			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
			{
				System.out.println("trans" + transferFlavors.length);
				return true;
			}
		});
	}
}
