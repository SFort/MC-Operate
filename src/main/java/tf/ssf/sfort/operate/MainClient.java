package tf.ssf.sfort.operate;

import net.fabricmc.api.ClientModInitializer;


public class MainClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JoltRenderer.register();
	}
}
