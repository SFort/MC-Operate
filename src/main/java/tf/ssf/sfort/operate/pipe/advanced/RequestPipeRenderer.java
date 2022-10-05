package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.pipe.AbstractPipeRenderer;

public class RequestPipeRenderer<T extends RequestPipeEntity> extends AbstractPipeRenderer<T> {

	public static void register() {
		if (Config.advancedPipe == null) return;
		BlockEntityRendererRegistry.register(RequestPipeEntity.ENTITY_TYPE, ctx -> new RequestPipeRenderer<>()::render);
	}

}
