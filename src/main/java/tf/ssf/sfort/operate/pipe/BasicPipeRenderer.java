package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

public class BasicPipeRenderer extends AbstractPipeRenderer {

	public static void register() {
		//if (Config.colorTube == null) return;
		BlockEntityRendererRegistry.register(BasicPipeEntity.ENTITY_TYPE, ctx -> new BasicPipeRenderer()::render);
	}

}
