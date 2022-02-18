package firok.pivi.util;

public class TimeUtil
{
	private static final Thread thread;
	private static volatile long now = System.currentTimeMillis();
	static
	{
		thread = new Thread(()->{
			while(true)
			{
				try
				{
					TimeUtil.now = System.currentTimeMillis();
					Thread.sleep(10);
				}
				catch (Exception ignored) { }
			}
		});
		thread.start();
	}

	public static long getNow()
	{
		return now;
	}
}
