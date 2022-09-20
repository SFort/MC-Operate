package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import tf.ssf.sfort.operate.Config;

public class OverseerPipeRenderer extends AbstractPipeRenderer<OverseerPipeEntity> {

	public static void register() {
		if (Config.basicPipe == null) return;
		BlockEntityRendererRegistry.register(OverseerPipeEntity.ENTITY_TYPE, ctx -> new OverseerPipeRenderer()::render);
	}

}
