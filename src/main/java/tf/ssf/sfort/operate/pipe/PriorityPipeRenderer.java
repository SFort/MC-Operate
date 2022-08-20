package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class PriorityPipeRenderer extends AbstractPipeRenderer<PriorityPipeEntity> {

	public static void register() {
		//if (Config.colorTube == null) return;
		BlockEntityRendererRegistry.register(PriorityPipeEntity.ENTITY_TYPE, ctx -> new PriorityPipeRenderer()::render);
	}

	@Override
	public void renderConnections(PriorityPipeEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		super.renderConnections(entity, tickDelta, matrix, vertex, light, overlay);
		for (Direction dir : Direction.values()) {
			matrix.push();
			matrix.translate(.5, .5, .5);
			matrix.push();
			matrix.multiply(dir.getRotationQuaternion());
			switch (dir) {
				case DOWN -> matrix.scale(1, 1, -1);
				case WEST, NORTH -> matrix.scale(-1, 1, 1);
			}
			if ((entity.connectedLowPrioritySides & (1 << dir.ordinal())) != 0) {
				drawSideLines(matrix.peek(), vertex.getBuffer(RenderLayer.LINES), .5f, .5f, .5f, .7f);
			}
			matrix.pop();
			matrix.pop();
		}
	}

}
