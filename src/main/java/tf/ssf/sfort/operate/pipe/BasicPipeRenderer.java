package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import tf.ssf.sfort.operate.Config;

public class BasicPipeRenderer <T extends BasicPipeEntity> extends AbstractPipeRenderer<T> {

	public static void register() {
		if (Config.basicPipe == null) return;
		BlockEntityRendererRegistry.register(BasicPipeEntity.ENTITY_TYPE, ctx -> new BasicPipeRenderer<>()::render);
	}

}
