package firok.pivi.util;

import java.awt.*;
import java.net.URI;

public class BrowserUtil
{
	public static void accessURI(URI uri)
	{
		try
		{
			var desktop = Desktop.getDesktop();
			if(desktop.isSupported(Desktop.Action.BROWSE))
			{
				desktop.browse(uri);
			}
		}
		catch (Exception err)
		{
			System.err.println("无法打开链接");
			err.printStackTrace(System.err);
		}
	}

	public static void accessURI(String uri)
	{
		try
		{
			accessURI(new URI(uri));
		}
		catch (Exception err)
		{
			System.err.println("无法打开链接");
			err.printStackTrace(System.err);
		}
	}


}
