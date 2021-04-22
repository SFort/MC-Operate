package tf.ssf.sfort;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;


public class MainClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JoltRenderer.register();
	}
}
