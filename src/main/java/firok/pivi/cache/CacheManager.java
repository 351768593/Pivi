package firok.pivi.cache;

import java.net.URL;
import java.util.UUID;

public class CacheManager
{
	public UUID cacheFor(URL url)
	{
		UUID uuid = UUID.randomUUID();


		return uuid;
	}

	public CacheStatus getCacheStatus(UUID uuid)
	{
		return CacheStatus.NotStarted;
	}

	public void cacelCache(UUID uuid)
	{
		;
	}
}
