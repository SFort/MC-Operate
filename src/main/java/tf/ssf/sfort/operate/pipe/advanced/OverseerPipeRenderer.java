package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.pipe.AbstractPipeRenderer;

public class OverseerPipeRenderer<T extends OverseerPipeEntity> extends AbstractPipeRenderer<T> {

	public static void register() {
		if (Config.advancedPipe == null) return;
		BlockEntityRendererRegistry.register(OverseerPipeEntity.ENTITY_TYPE, ctx -> new OverseerPipeRenderer<>()::render);
	}

}
