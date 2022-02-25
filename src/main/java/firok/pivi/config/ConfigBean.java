package firok.pivi.config;

import lombok.Data;
import lombok.ToString;

import java.awt.*;
import java.io.*;
import java.util.Scanner;

@Data
@ToString
public class ConfigBean
{
	ConfigZoomMode initZoomMode = ConfigZoomMode.OriginSize;
	Integer initZoomPercent = 100;

	String lafClassName = com.formdev.flatlaf.FlatDarkLaf.class.getName();

	Integer initLocX = 600, initLocY = 400;
	Integer initWidth = 366, initHeight = 366;
	Integer initFrameState = Frame.MAXIMIZED_BOTH;

	Integer zoomSpeed = 40;

	Integer beaconPort = 21005;

	Long timestamp = Long.MIN_VALUE;

	public void override(ConfigBean bean)
	{
		if(bean.initZoomMode != null) this.initZoomMode = bean.initZoomMode;
		if(bean.initZoomPercent != null) this.initZoomPercent = bean.initZoomPercent;
		if(bean.lafClassName != null) this.lafClassName = bean.lafClassName;

		if(bean.initLocX != null) this.initLocX = bean.initLocX;
		if(bean.initLocY != null) this.initLocY = bean.initLocY;
		if(bean.initWidth != null) this.initWidth = bean.initWidth;
		if(bean.initHeight != null) this.initHeight = bean.initHeight;
		if(bean.initFrameState != null) this.initFrameState = bean.initFrameState;

		if(bean.beaconPort != null) this.beaconPort = bean.beaconPort;

		if(bean.timestamp != null) this.timestamp = bean.timestamp;
	}

	public static ConfigBean fromFile(File file) throws IOException
	{
		var ret = new ConfigBean();

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
					case "initZoomMode" -> ret.initZoomMode = ConfigZoomMode.valueOf(value);
					case "initZoomPercent" -> ret.initZoomPercent = Integer.valueOf(value);
					case "lafClassName" -> ret.lafClassName = value;

					case "initLocX" -> ret.initLocX = Integer.valueOf(value);
					case "initLocY" -> ret.initLocY = Integer.valueOf(value);
					case "initWidth" -> ret.initWidth = Integer.valueOf(value);
					case "initHeight" -> ret.initHeight = Integer.valueOf(value);
					case "initFrameState" -> ret.initFrameState = Integer.valueOf(value);

					case "beaconPort" -> ret.beaconPort = Integer.valueOf(value);

					case "timestamp" -> ret.timestamp = Long.valueOf(value);
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
			out.println("initZoomMode="+initZoomMode);
			out.println("initZoomPercent="+initZoomPercent);
			out.println("lafClassName="+lafClassName);
			out.println("initLocX="+initLocX);
			out.println("initLocY="+initLocY);
			out.println("initWidth="+initWidth);
			out.println("initHeight="+initHeight);
			out.println("initFrameState="+ initFrameState);
			out.println("timestamp="+timestamp);
			out.println("beaconPort"+beaconPort);

			out.flush();
			ofs.flush();
		}
	}
}
