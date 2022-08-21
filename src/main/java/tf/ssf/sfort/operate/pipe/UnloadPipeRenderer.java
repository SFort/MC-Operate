package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import tf.ssf.sfort.operate.Config;

public class UnloadPipeRenderer extends AbstractPipeRenderer {

	public static void register() {
		if (Config.basicPipe == null) return;
		BlockEntityRendererRegistry.register(UnloadPipeEntity.ENTITY_TYPE, ctx -> new UnloadPipeRenderer()::render);
	}

}
