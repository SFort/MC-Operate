package tf.ssf.sfort.operate;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import tf.ssf.sfort.operate.client.FakeRequestScreen;
import tf.ssf.sfort.operate.jolt.JoltRenderer;
import tf.ssf.sfort.operate.pipe.BasicPipeRenderer;
import tf.ssf.sfort.operate.pipe.EntrancePipeRenderer;
import tf.ssf.sfort.operate.pipe.FilterPipeRenderer;
import tf.ssf.sfort.operate.pipe.PriorityPipeRenderer;
import tf.ssf.sfort.operate.pipe.UnloadPipeRenderer;
import tf.ssf.sfort.operate.pipe.advanced.ExchangePipeRenderer;
import tf.ssf.sfort.operate.pipe.advanced.OverseerPipeRenderer;
import tf.ssf.sfort.operate.pipe.advanced.RequestPipeRenderer;
import tf.ssf.sfort.operate.punch.PunchRenderer;
import tf.ssf.sfort.operate.cylinder.CylinderRenderer;
import tf.ssf.sfort.operate.tube.ColorTubeRenderer;


public class MainClient implements ClientModInitializer {
	public static final MinecraftClient mc = MinecraftClient.getInstance();
	public static FakeRequestScreen requestScreen = null;

	public static void requestPipeKeyboardHack() {
		KeyBinding.unpressAll();
		requestScreen = new FakeRequestScreen().getSelf();
		if (requestScreen != null) requestScreen.init(mc, mc.getWindow().getWidth(), mc.getWindow().getHeight());
	}

	public static Screen getRequestScreen() {
		if (requestScreen == null) return requestScreen;
		requestScreen = requestScreen.getSelf();
		return requestScreen;
	}

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
		ExchangePipeRenderer.register();
		RequestPipeRenderer.register();
		//BitStakRenderer.register();
	}
	public static int getHorizontalPlayerFacing() {
		PlayerEntity pe = mc.player;
		if (pe != null)
			return pe.getHorizontalFacing().getHorizontal();
		return -1;
	}

}
