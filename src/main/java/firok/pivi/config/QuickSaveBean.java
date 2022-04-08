package firok.pivi.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

@Data
@ToString
public class QuickSaveBean
{
	java.util.List<Mapping> listMapping;

	@AllArgsConstructor public static class Mapping { public String name; public File value; }

	public static QuickSaveBean fromFile(File file)
	{
		var ret = new QuickSaveBean();
		ret.listMapping = new ArrayList<>();

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

				final String name = tempWords[0].trim(), raw = tempWords[1].trim();
				final String value = raw.startsWith("\"") && raw.endsWith("\"") ? raw.substring(1, raw.length() - 1) : raw;

				var folder = new File(value);

				ret.listMapping.add(new Mapping(name, folder.getAbsoluteFile()));
			}
		}
		catch (Exception ignored) { }

		return ret;
	}
}
