package firok.pivi.util;

/**
 * https://blog.csdn.net/qq_41054313/article/details/88424454
 */
public class BitUtil
{
	public static byte[] toBytes(int n)
	{
		byte[] b = new byte[4];
		b[3] = (byte) (n & 0xff);
		b[2] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n >> 16 & 0xff);
		b[0] = (byte) (n >> 24 & 0xff);
		return b;
	}

	public static int toInt(byte[] b)
	{
		int res = 0;
		for(int i=0; i<b.length; i++)
		{
			res += (b[i] & 0xff) << ((3-i)*8);
		}
		return res;
	}

	public static boolean equalBuffer(byte[] buffer1, byte[] buffer2)
	{
		if(buffer1 == buffer2) return true;
		if(buffer1 == null || buffer2 == null) return false;
		if(buffer1.length != buffer2.length) return false;
		for(int step = 0; step < buffer1.length; step++)
		{
			if(buffer1[step] != buffer2[step]) return false;
		}

		return true;
	}
}
