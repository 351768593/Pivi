package firok.pivi.config;

import lombok.Data;
import lombok.ToString;

import java.io.*;
import java.util.Scanner;

@Data
@ToString
public class ConfigBean
{
	Boolean isInitLayoutURL = false;
	Boolean isInitLayoutOperationBar = false;
	Boolean isInitLayoutImageInfo = false;
	Boolean isInitLayoutScrollBar = false;

	ConfigZoomMode initZoomMode = ConfigZoomMode.OriginSize;
	Integer initZoomPercent = 100;

	Boolean isAnimationLoadingParticle = true;
	Boolean isAnimationZoom = true;

	String lafClassName = com.formdev.flatlaf.FlatDarkLaf.class.getName();

	public void override(ConfigBean bean)
	{
		if(bean.isInitLayoutURL != null) this.isInitLayoutURL = bean.isInitLayoutURL;
		if(bean.isInitLayoutOperationBar != null) this.isInitLayoutOperationBar = bean.isInitLayoutOperationBar;
		if(bean.isInitLayoutImageInfo != null) this.isInitLayoutImageInfo = bean.isInitLayoutImageInfo;
		if(bean.isInitLayoutScrollBar != null) this.isInitLayoutScrollBar = bean.isInitLayoutScrollBar;
		if(bean.initZoomMode != null) this.initZoomMode = bean.initZoomMode;
		if(bean.initZoomPercent != null) this.initZoomPercent = bean.initZoomPercent;
		if(bean.isAnimationLoadingParticle != null) this.isAnimationLoadingParticle = bean.isAnimationLoadingParticle;
		if(bean.isAnimationZoom != null) this.isAnimationZoom = bean.isAnimationZoom;
		if(bean.lafClassName != null) this.lafClassName = bean.lafClassName;
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
					case "isInitLayoutURL" -> ret.isInitLayoutURL = Boolean.parseBoolean(value);
					case "isInitLayoutOperationBar" -> ret.isInitLayoutOperationBar = Boolean.parseBoolean(value);
					case "isInitLayoutImageInfo" -> ret.isInitLayoutImageInfo = Boolean.parseBoolean(value);
					case "isInitLayoutScrollBar" -> ret.isInitLayoutScrollBar = Boolean.parseBoolean(value);
					case "initZoomMode" -> ret.initZoomMode = ConfigZoomMode.valueOf(value);
					case "initZoomPercent" -> ret.initZoomPercent = Integer.valueOf(value);
					case "isAnimationLoadingParticle" -> ret.isAnimationLoadingParticle = Boolean.parseBoolean(value);
					case "isAnimationZoom" -> ret.isAnimationZoom = Boolean.parseBoolean(value);
					case "lafClassName" -> ret.lafClassName = value;
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
			out.println("isInitLayoutURL="+isInitLayoutURL);
			out.println("isInitLayoutOperationBar="+isInitLayoutOperationBar);
			out.println("isInitLayoutImageInfo="+isInitLayoutImageInfo);
			out.println("isInitLayoutScrollBar="+isInitLayoutScrollBar);
			out.println("initZoomMode="+initZoomMode);
			out.println("initZoomPercent="+initZoomPercent);
			out.println("isAnimationLoadingParticle="+isAnimationLoadingParticle);
			out.println("isAnimationZoom="+isAnimationZoom);
			out.println("lafClassName="+lafClassName);

			out.flush();
			ofs.flush();
		}
	}
}
