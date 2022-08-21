package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import tf.ssf.sfort.operate.Config;

public class EntrancePipeRenderer extends AbstractPipeRenderer {

	public static void register() {
		if (Config.basicPipe == null) return;
		BlockEntityRendererRegistry.register(EntrancePipeEntity.ENTITY_TYPE, ctx -> new EntrancePipeRenderer()::render);
	}

}
