package firok.pivi.config;

import lombok.Data;
import lombok.ToString;

import java.io.*;
import java.util.Scanner;

@Data
@ToString
public class RuntimeBean
{
	Integer initLocX = 600, initLocY = 400;
	Integer initWidth = 366, initHeight = 366;

	public void override(RuntimeBean runtime)
	{
		if(runtime.initLocX != null) this.initLocX = runtime.initLocX;
		if(runtime.initLocY != null) this.initLocY = runtime.initLocY;
		if(runtime.initWidth != null) this.initWidth = runtime.initWidth;
		if(runtime.initHeight != null) this.initHeight = runtime.initHeight;
	}

	public static RuntimeBean fromFile(File file) throws IOException
	{
		var ret = new RuntimeBean();

		try(
			var ifs = new FileInputStream(file);
			var in = new Scanner(ifs)
		)
		{
			while(in.hasNextLine())
			{
				String tempLine = in.nextLine();
				if(tempLine.indexOf('=') < 0) continue;
				String[] tempWords = tempLine.split("=");
				if(tempWords.length != 2) continue;

				final String key = tempWords[0].trim(), value = tempWords[1].trim();
				switch (key)
				{
					case "initLocX" -> ret.initLocX = Integer.valueOf(value);
					case "initLocY" -> ret.initLocY = Integer.valueOf(value);
					case "initWidth" -> ret.initWidth = Integer.valueOf(value);
					case "initHeight" -> ret.initHeight = Integer.valueOf(value);
				}
			}
		}

		return ret;
	}

	public void toFile(File file) throws IOException
	{
		try(
			var ofs = new FileOutputStream(file);
			var out = new PrintWriter(ofs)
		)
		{
			out.println("initLocX="+initLocX);
			out.println("initLocY="+initLocY);
			out.println("initWidth="+initWidth);
			out.println("initHeight="+initHeight);

			out.flush();
			ofs.flush();
		}
	}
}
