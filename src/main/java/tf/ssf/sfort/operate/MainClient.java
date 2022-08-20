package tf.ssf.sfort.operate;

import net.fabricmc.api.ClientModInitializer;
import tf.ssf.sfort.operate.jolt.JoltRenderer;
import tf.ssf.sfort.operate.pipe.BasicPipeRenderer;
import tf.ssf.sfort.operate.pipe.EntrancePipeRenderer;
import tf.ssf.sfort.operate.pipe.FilterPipeRenderer;
import tf.ssf.sfort.operate.pipe.PriorityPipeRenderer;
import tf.ssf.sfort.operate.pipe.UnloadPipeRenderer;
import tf.ssf.sfort.operate.punch.PunchRenderer;
import tf.ssf.sfort.operate.tube.ColorTubeRenderer;


public class MainClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JoltRenderer.register();
		PunchRenderer.register();
		ColorTubeRenderer.register();
		BasicPipeRenderer.register();
		EntrancePipeRenderer.register();
		UnloadPipeRenderer.register();
		PriorityPipeRenderer.register();
		FilterPipeRenderer.register();
		//BitStakRenderer.register();
	}
}
