package firok.pivi.util;

import lombok.Getter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于解析传入参数并执行若干操作
 */
public class ArgumentCompact
{
	public boolean modeVersion;

	public boolean modeHelp;

	public boolean modeGenWin;

	/**
	 * beacon port
	 */
	public Integer port;

	public List<String> listPotentialUrl = new ArrayList<>(3);

	public ArgumentCompact(String[] args)
	{
		if(args == null || args.length == 0) return;

		for(int step = 0; step < args.length; step++)
		{
			var arg = args[step];
			switch (arg)
			{
				case "-p", "-port" -> {
					if(port == null && hasNext(args, step, 1))
					{
						step++;
						port = Integer.valueOf(args[step]);
					}
				}
				case "-h", "-help" -> modeHelp = true;
				case "-v", "-version" -> modeVersion = true;
				case "-gen-win" -> modeGenWin = true;
				default -> listPotentialUrl.add(arg);
			}
		}
	}

	private static boolean hasNext(String[] args, int from, int length)
	{
		return from + length < args.length;
	}
}
