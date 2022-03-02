package firok.pivi.beacon;

import firok.pivi.Pivi;
import firok.pivi.util.BitUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BeaconDispatchThread extends Thread
{
	Socket socket;
	public BeaconDispatchThread(Socket socket)
	{
		super();
		this.socket = socket;
	}

	static final byte[] bufferSuccess = new byte[] { 0x3E, 0x7, 0x17, 0x47 };
	static final byte[] bufferError = new byte[] { 0xE, 0x9, 0x3, 0x51 };

	private void shutdown()
	{
		try { this.socket.shutdownInput(); } catch (Exception ignored) { }
		try { this.socket.shutdownOutput(); } catch (Exception ignored) { }
		try { this.socket.close(); } catch (Exception ignored) { }
	}

	@Override
	public void run()
	{
		InputStream is;
		OutputStream os;

		try
		{
			is = socket.getInputStream();
			os = socket.getOutputStream();
		}
		catch (Exception e)
		{
			System.err.println("信标: 创建流失败");
			this.shutdown();
			return;
		}

		boolean success;
		try
		{
			byte[] bufferLength = new byte[4];
			is.read(bufferLength);
			final int length = BitUtil.toInt(bufferLength);

			byte[] bufferUrl = new byte[length];
			is.read(bufferUrl);
			String strUrl = new String(bufferUrl, StandardCharsets.UTF_8);
			Pivi.initFrameImage(strUrl);

			System.out.println("信标接收数据完成");
			success = true;
		}
		catch (Exception e)
		{
			System.err.println("信标接收数据失败");
			e.printStackTrace(System.err);
			success = false;
		}

		try
		{
			os.write(success ? bufferSuccess : bufferError);
			os.flush();
		}
		catch (Exception ignored) { }

		this.shutdown();
	}
}
