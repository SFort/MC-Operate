package tf.ssf.sfort.operate.pipe;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.util.SyncableLinkedList;

public class AbstractPipeRenderer<T extends AbstractPipeEntity> {

	public static final Quaternion X_ROTATION = Vec3f.POSITIVE_Y.getDegreesQuaternion(90);

	public void render(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		renderConnections(entity, tickDelta, matrix, vertex, light, overlay);
		renderItems(entity, tickDelta, matrix, vertex, light, overlay);
	}
	public void renderItems(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		ItemRenderer ir = MinecraftClient.getInstance().getItemRenderer();
		World world = entity.getWorld();
		if (world == null) return;
		final float step = 1 / (float) entity.getPipeTransferTime();
		for (SyncableLinkedList.Node<TransportedStack> stackNode = entity.itemQueue.first; stackNode!=null; stackNode=stackNode.next) {
			TransportedStack entry = stackNode.item;
			matrix.push();
			long diff = Math.min(entity.getPipeTransferTime(), entry.travelTime - world.getTime());
			double progress = 0;
			if (diff > 0) {
				float divDiff = diff / (float)entity.getPipeTransferTime();
				progress = MathHelper.lerp(tickDelta, divDiff + step, divDiff);
			}
			switch (entry.origin) {
				case UP:
					matrix.translate(.5, .5 + progress, .37);
					break;
				case DOWN:
					matrix.translate(.5, .5 - progress, .63);
					break;
				case NORTH:
					matrix.translate(.37, .45, .5 - progress);
					matrix.multiply(X_ROTATION);
					break;
				case SOUTH:
					matrix.translate(.63, .45, .5 + progress);
					matrix.multiply(X_ROTATION);
					break;
				case WEST:
					matrix.translate(.5 - progress, .45, .37);
					break;
				case EAST:
					matrix.translate(.5 + progress, .45, .63);
					break;
			}
			matrix.push();
			matrix.scale(.5f, .5f, .5f);
			ir.renderItem(null, entry.stack, ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
			matrix.pop();
		}
	}
	public void renderConnections(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
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
			if ((entity.connectedSides & (1 << dir.ordinal())) != 0) {
				drawSideLines(matrix.peek(), vertex.getBuffer(RenderLayer.LINES), 1, 1, 1, .7f);
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
	public void renderDisconnects(AbstractPipeEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay, Direction dir) {
		DisconnectedSideLinesRenderer renderer = entity.getDisconnectedSideLinesRenderer(dir);
		if (renderer != null) renderer.draw(matrix.peek(), vertex.getBuffer(RenderLayer.LINES), .5f, .5f, .5f, .8f);
	}

	public interface DisconnectedSideLinesRenderer {
		void draw(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha);
	}
	public static void drawDisconnectedSideLines(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha) {
		drawLine(entry, vertexConsumer, -.125f, .5f, -.125f, .125f, .75f, -.125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .5f, -.125f, -.125f, .75f, -.125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, -.125f, .5f, .125f, .125f, .75f, .125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .5f, .125f, -.125f, .75f, .125f, 0, 0, 0, red, green, blue, alpha);

		drawLine(entry, vertexConsumer, -.125f, .5f, .125f, -.125f, .75f, -.125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, -.125f, .5f, -.125f, -.125f, .75f, .125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .5f, .125f, .125f, .75f, -.125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .5f, -.125f, .125f, .75f, .125f, 0, 0, 0, red, green, blue, alpha);
	}
	public static void drawSideLines(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha) {
		drawLine(entry, vertexConsumer, -.125f, .25f, -.125f, -.125f, .5f, -.125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .25f, -.125f, .125f, .5f, -.125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, -.125f, .25f, .125f, -.125f, .5f, .125f, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .25f, .125f, .125f, .5f, .125f, 0, 0, 0, red, green, blue, alpha);
	}

	public static void drawLine(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
		float k = (maxX - minX);
		float l = (maxY - minY);
		float m = (maxZ - minZ);
		float n = MathHelper.sqrt(k * k + l * l + m * m);
		k /= n;
		l /= n;
		m /= n;
		vertexConsumer.vertex(entry.getPositionMatrix(), (float) (minX + offsetX), (float) (minY + offsetY), (float) (minZ + offsetZ)).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), k, l, m).next();
		vertexConsumer.vertex(entry.getPositionMatrix(), (float) (maxX + offsetX), (float) (maxY + offsetY), (float) (maxZ + offsetZ)).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), k, l, m).next();
	}

}
