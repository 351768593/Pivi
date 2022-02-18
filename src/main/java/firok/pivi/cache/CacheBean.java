package firok.pivi.cache;

import firok.pivi.util.TimeUtil;
import lombok.Data;

import java.io.File;
import java.net.URL;
import java.util.UUID;

@Data
public class CacheBean
{
	public final UUID uuid = UUID.randomUUID();
	public final long timeStarted = TimeUtil.getNow();

	URL url;
	CacheStatus status = CacheStatus.NotStarted;
	long timeStopped = Long.MIN_VALUE;

	public File getFile()
	{
		return new File("./pivi_cache/"+uuid);
	}
}
