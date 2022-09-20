package tf.ssf.sfort.operate;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import tf.ssf.sfort.operate.jolt.JoltRenderer;
import tf.ssf.sfort.operate.pipe.BasicPipeRenderer;
import tf.ssf.sfort.operate.pipe.EntrancePipeRenderer;
import tf.ssf.sfort.operate.pipe.FilterPipeRenderer;
import tf.ssf.sfort.operate.pipe.OverseerPipeRenderer;
import tf.ssf.sfort.operate.pipe.PriorityPipeRenderer;
import tf.ssf.sfort.operate.pipe.UnloadPipeRenderer;
import tf.ssf.sfort.operate.punch.PunchRenderer;
import tf.ssf.sfort.operate.stak.cylinder.CylinderRenderer;
import tf.ssf.sfort.operate.tube.ColorTubeRenderer;


public class MainClient implements ClientModInitializer {
	public static final MinecraftClient mc = MinecraftClient.getInstance();

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
		CylinderRenderer.register();
		OverseerPipeRenderer.register();
		//BitStakRenderer.register();
	}
}
