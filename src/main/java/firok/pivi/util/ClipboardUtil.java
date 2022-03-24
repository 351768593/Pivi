package firok.pivi.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardUtil
{
	static final Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
	public static void putTextIntoClipboard(String value)
	{
		var trans = new StringSelection(value);
		cp.setContents(trans, null);
	}
	public static void putImageIntoClipboard(Image value)
	{
	}

	public static Image getImageFromClipboard()
	{
		var trans = cp.getContents(null);
		if(trans == null || !trans.isDataFlavorSupported(DataFlavor.imageFlavor)) return null;

		try
		{
			return (Image) trans.getTransferData(DataFlavor.imageFlavor);
		}
		catch (Exception ignored)
		{
			return null;
		}
	}
}
