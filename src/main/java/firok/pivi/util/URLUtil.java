package firok.pivi.util;

import java.io.File;
import java.net.URL;

public class URLUtil
{
	/**
	 * @param raw 原始字符串
	 * @return 如果能识别为文件路径或URL则返回, 否则返回null
	 */
	public static URL readUrl(String raw)
	{
		try
		{
			var file = new File(raw);
			if(file.exists() && file.isFile())
				return file.toURL();
			else throw new RuntimeException();
		}
		catch (Exception exceptionFromFile)
		{
			try { return new URL(raw); }
			catch (Exception ignored) { }
		}

		return null;
	}
}
