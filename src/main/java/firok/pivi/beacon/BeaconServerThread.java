package firok.pivi.beacon;

import java.io.IOException;
import java.net.ServerSocket;

public class BeaconServerThread extends Thread
{
	private final int port;
	private ServerSocket socketServer;
	public BeaconServerThread(int port) throws IOException
	{
		super();
		if(port <= 0 || port > 65535) throw new IllegalArgumentException("错误的端口号");

		this.setDaemon(true);

		this.port = port;
		this.socketServer = new ServerSocket(port);
		this.socketServer.setReceiveBufferSize(1024);
	}

	public void run()
	{
		do
		{
			try
			{
				var socketNew = socketServer.accept();
				var threadDispatch = new BeaconDispatchThread(socketNew);
				threadDispatch.setDaemon(true);
				threadDispatch.start();
				System.out.println("server socket created");
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
			}

		}
		while(true);
	}
}
