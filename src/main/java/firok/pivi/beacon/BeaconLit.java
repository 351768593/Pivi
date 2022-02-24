package firok.pivi.beacon;

import firok.pivi.util.BitUtil;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BeaconLit
{
	public static boolean litServer(int port)
	{
		try
		{
			var threadServer = new BeaconServerThread(port);
			threadServer.start();

			return true;
		}
		catch (Exception e)
		{
			System.err.println("点亮信标失败");
			e.printStackTrace(System.err);
			return false;
		}
	}

	public static boolean litClient(int port, String url)
	{
		try
		{
			var socketClient = new Socket("localhost", port);

			try(var is = socketClient.getInputStream();
			    var os = socketClient.getOutputStream())
			{
				byte[] bufferUrl = url.getBytes(StandardCharsets.UTF_8);
				byte[] bufferLength = BitUtil.toBytes(bufferUrl.length);

				os.write(bufferLength);
				os.write(bufferUrl);
				os.flush();

				byte[] bufferResult = new byte[4];
				is.read(bufferResult);
				return true;
			}
		}
		catch (Exception e)
		{
			System.err.println("连接信标失败");
			e.printStackTrace(System.err);
			return false;
		}
	}
}
