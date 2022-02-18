package firok.pivi.scene;

import firok.pivi.Pivi;

import java.util.Random;

class ThreadSceneLoadingParticle extends Thread
{
	SceneManager smgr;
	ThreadSceneLoadingParticle(SceneManager smgr)
	{
		super();
		this.setDaemon(true);
		this.smgr = smgr;
	}

	@Override
	public void run()
	{
		int interval = 1;
		var rand = new Random();
		var form = Pivi.piviInstance;
		var pScene = form.pScene;
		while(true)
		{
			int width = pScene.getWidth();
			if(Pivi.config.getIsAnimationLoadingParticle() && interval % 5 == 1)
			{
				var size = rand.nextInt() % 20 + 10;
				var factor = 0.0725 - 0.0525 * size / 30;
				var color =
						(((55 + rand.nextInt(200)) & 0xFF) << 16) |
								((55 + rand.nextInt(200) & 0xFF) << 8)  |
								((55 + rand.nextInt(200) & 0xFF) << 0);
				var en = new EntityParticleLoading(
						rand.nextInt() % width,
						0,
						color,
						size,
						factor
				);
				smgr.addEntity(en);
			}
			try
			{
				Thread.sleep(50);
			}
			catch (Exception ignored) { }

			interval ++;
			if(interval >= 1000) interval = 0;
		}
	}
}
