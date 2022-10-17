package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;

public class PriorityPipeRenderer<T extends PriorityPipeEntity> extends AbstractPipeRenderer<T> {

	public static void register() {
		if (Config.basicPipe == null) return;
		BlockEntityRendererRegistry.register(PriorityPipeEntity.ENTITY_TYPE, ctx -> new PriorityPipeRenderer<>()::render);
	}

	@Override
	public void renderConnections(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		super.renderConnections(entity, tickDelta, matrix, vertex, light, overlay);
		World world = entity.getWorld();
		BlockPos.Mutable pos = entity.getPos().mutableCopy();
		for (Direction dir : Direction.values()) {
			matrix.push();
			matrix.translate(.5, .5, .5);
			matrix.push();
			matrix.multiply(dir.getRotationQuaternion());
			switch (dir) {
				case DOWN -> matrix.scale(1, 1, -1);
				case WEST, NORTH -> matrix.scale(-1, 1, 1);
			}
			if ((entity.connectedLowPrioritySidesByte & (1 << dir.ordinal())) != 0) {
				drawSideLines(matrix.peek(), vertex.getBuffer(RenderLayer.LINES), .5f, .5f, .5f, .7f);
				pos.set(entity.getPos()).move(dir);
				if (world != null) {
					BlockEntity nentity = world.getBlockEntity(pos);
					if (nentity instanceof AbstractPipeEntity) {
						renderDisconnects((AbstractPipeEntity) nentity, tickDelta, matrix, vertex, light, overlay, dir.getOpposite());
					}
				}
			}
			matrix.pop();
			matrix.pop();
		}
	}

}
