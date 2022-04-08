package firok.pivi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class ResourceUtil
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

	/**
	 * @param url 需要读取的目标路径
	 * @return 读取到的所有字节 如果出现错误就返回null
	 */
	public static byte[] readBytes(URL url)
	{
		try
		{
			try(var is = url.openStream())
			{
				return is.readAllBytes();
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}


	/**
	 * @param file 要写入的指定文件
	 * @param bytes 要写入的数据内容
	 * @return 是否写入成功
	 */
	public static boolean writeBytes(File file, byte[] bytes)
	{
		try
		{
			try(var ofs = new FileOutputStream(file))
			{
				ofs.write(bytes);
				ofs.flush();
			}
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private static final char[] charsNumber = new char[] {
			'0', '1', '2', '3',
			'4', '5', '6', '7',
			'8', '9', 'A', 'B',
			'C', 'D', 'E', 'F',
	};

	/**
	 * 通过前几个字节内容来判断是否是gif动图
	 *
	 *
	 * https://blog.csdn.net/qq_35491812/article/details/89951841
	 *
	 * 常用文件的文件头如下：(以前六位为准)
	 * JPEG (jpg)，文件头：FFD8FF
	 * PNG (png)，文件头：89504E47
	 * GIF (gif)，文件头：47494638
	 * TIFF (tif)，文件头：49492A00
	 * Windows Bitmap (bmp)，文件头：424D
	 * CAD (dwg)，文件头：41433130
	 * Adobe Photoshop (psd)，文件头：38425053
	 * Rich Text Format (rtf)，文件头：7B5C727466
	 * XML (xml)，文件头：3C3F786D6C
	 * HTML (html)，文件头：68746D6C3E
	 * Email [thorough only] (eml)，文件头：44656C69766572792D646174653A
	 * Outlook Express (dbx)，文件头：CFAD12FEC5FD746F
	 * Outlook (pst)，文件头：2142444E
	 * MS Word/Excel (xls.or.doc)，文件头：D0CF11E0
	 * MS Access (mdb)，文件头：5374616E64617264204A
	 * WordPerfect (wpd)，文件头：FF575043
	 * Postscript (eps.or.ps)，文件头：252150532D41646F6265
	 * Adobe Acrobat (pdf)，文件头：255044462D312E
	 * Quicken (qdf)，文件头：AC9EBD8F
	 * Windows Password (pwl)，文件头：E3828596
	 * ZIP Archive (zip)，文件头：504B0304
	 * RAR Archive (rar)，文件头：52617221
	 * Wave (wav)，文件头：57415645
	 * AVI (avi)，文件头：41564920
	 * Real Audio (ram)，文件头：2E7261FD
	 * Real Media (rm)，文件头：2E524D46
	 * MPEG (mpg)，文件头：000001BA
	 * MPEG (mpg)，文件头：000001B3
	 * Quicktime (mov)，文件头：6D6F6F76
	 * Windows Media (asf)，文件头：3026B2758E66CF11
	 * MIDI (mid)，文件头：4D546864
	 */
	public static boolean isAnimatedImage(byte[] bytes)
	{
		var header = getDataHeader(bytes);
		var fileType = getFileTypeFromDataHeader(header);
		return fileType == FileType.GIF;
	}

	public static String getDataHeader(byte[] bytes)
	{
		if(bytes == null || bytes.length < 4) return null;

		byte[] bytesHeaderRaw = new byte[] {
				bytes[0],
				bytes[1],
				bytes[2],
				bytes[3],
		};
		byte[] bytesHeaderHex = new byte[] {
				(byte)((bytesHeaderRaw[0] >> 4) & 0xF),
				(byte)((bytesHeaderRaw[0]     ) & 0xF),
				(byte)((bytesHeaderRaw[1] >> 4) & 0xF),
				(byte)((bytesHeaderRaw[1]     ) & 0xF),
				(byte)((bytesHeaderRaw[2] >> 4) & 0xF),
				(byte)((bytesHeaderRaw[2]     ) & 0xF),
				(byte)((bytesHeaderRaw[3] >> 4) & 0xF),
				(byte)((bytesHeaderRaw[3]     ) & 0xF),
		};
		char[] charsHex = new char[] {
				charsNumber[bytesHeaderHex[0]],
				charsNumber[bytesHeaderHex[1]],
				charsNumber[bytesHeaderHex[2]],
				charsNumber[bytesHeaderHex[3]],
				charsNumber[bytesHeaderHex[4]],
				charsNumber[bytesHeaderHex[5]],
				charsNumber[bytesHeaderHex[6]],
				charsNumber[bytesHeaderHex[7]],
		};
		return new String(charsHex);
	}
	public static FileType getFileTypeFromDataHeader(String header)
	{
		if(header == null) return FileType.Unknown;
		for(var objType : FileType.values())
		{
			if(objType.equalHeader(header))
			{
				return objType;
			}
		}
		return FileType.Unknown;
	}

	public enum FileType
	{
		JPEG("FFD8FF"),
		PNG("89504E47"),
		GIF("47494638", true),
		TIFF("49492A00"),
		BMP("424D"),
		CAD("41433130"),
		PSD("38425053"),
		RTF("7B5C727466"),
		XML("3C3F786D6C"),
		HTML("68746D6C3E"),
		EML("44656C69766572792D646174653A"),
		DBX("CFAD12FEC5FD746F"),
		PST("2142444E"),
		XLS_DOC("D0CF11E0"),
		MDB("5374616E64617264204A"),
		WPD("FF575043"),
		EPS_PS("252150532D41646F6265"),
		PDF("255044462D312E"),
		QDF("AC9EBD8F"),
		PWL("E3828596"),
		ZIP("504B0304"),
		RAR("52617221"),
		WAVE("57415645"),
		AVI("41564920"),
		RAM("2E7261FD"),
		RM("2E524D46"),
		MPEG("000001BA"),
		MPEG2("000001B3"),
		MOV("6D6F6F76"),
		ASF("3026B2758E66CF11"),
		MIDI("4D546864"),

		Unknown("        "),
		;
		public final String header;
		public final boolean isAnimatedImage;
		FileType(String header)
		{
			this(header, false);
		}
		FileType(String header, boolean isAnimatedImage)
		{
			this.header = header;
			this.isAnimatedImage = isAnimatedImage;
		}

		public boolean equalHeader(String header)
		{
			if(header == null) return false;
			return this.header.startsWith(header);
		}
	}
}
