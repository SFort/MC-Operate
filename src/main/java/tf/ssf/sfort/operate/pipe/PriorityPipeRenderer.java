package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import tf.ssf.sfort.operate.Config;

public class PriorityPipeRenderer<T extends PriorityPipeEntity> extends AbstractPipeRenderer<T> {

	public static void register() {
		if (Config.basicPipe == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BlockEntityRendererRegistry.register(PriorityPipeEntity.ENTITY_TYPE, ctx -> new PriorityPipeRenderer<>()::render);
	}

	@Override
	public void renderConnections(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		super.renderConnections(entity, tickDelta, matrix, vertex, light, overlay);
		renderConnections(entity.getWorld(), entity.getPos(), entity.connectedLowPrioritySidesByte, tickDelta, matrix, vertex, light, overlay, 0, 0, 0, 1, .5f, .5f, .5f, .7f);
	}

}
