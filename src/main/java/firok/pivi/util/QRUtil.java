package firok.pivi.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRUtil
{
	/**
	 * 从一张图片里扫描多个QR码
	 */
	public static List<Result> scanMultiQRCode(BufferedImage input)
	{
		try
		{
			var reader = new QRCodeMultiReader();
			var source = new BufferedImageLuminanceSource(input);
			var hb = new HybridBinarizer(source);
			var bitmap = new BinaryBitmap(hb);

			return Arrays.asList(reader.decodeMultiple(bitmap, DECODE_HINTS));
		}
		catch (NotFoundException e)
		{
			return null;
		}
	}

	/**
	 * 从一张图片里扫描一个二维码或条形码
	 */
	public static Result scanSingleMultiCode(BufferedImage input)
	{
		try
		{
			var reader = new MultiFormatReader();
			var source = new BufferedImageLuminanceSource(input);
			var hb = new HybridBinarizer(source);
			var bitmap= new BinaryBitmap(hb);

			return reader.decode(bitmap, DECODE_HINTS);
		}
		catch (NotFoundException e)
		{
			return null;
		}
	}



	private static final Map<DecodeHintType, Object> DECODE_HINTS = new HashMap<>();
	static
	{
		DECODE_HINTS.put(DecodeHintType.CHARACTER_SET, "UTF-8");
	}
}
