package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.pipe.AbstractPipeRenderer;
import tf.ssf.sfort.operate.pipe.advanced.util.RequestPipeUi;

import java.util.Map;

import static tf.ssf.sfort.operate.MainClient.mc;

public class RequestPipeRenderer<T extends RequestPipeEntity> extends AbstractPipeRenderer<T> {

	public static void register() {
		if (Config.advancedPipe == null) return;
		BlockEntityRendererRegistry.register(RequestPipeEntity.ENTITY_TYPE, ctx -> new RequestPipeRenderer<>()::render);
	}
	public void render(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		super.render(entity, tickDelta, matrix, vertex, light, overlay);
		request$render(entity, tickDelta, matrix, vertex, light, overlay);
	}
	public void request$render(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		if (entity.rpui == null) return;
		matrix.push();

		if (mc.player != null) {
			Direction dir = mc.player.getHorizontalFacing();
			matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-dir.asRotation()));
			if (dir == Direction.WEST) {
				matrix.translate(0, 0, -1);
			} else if (dir == Direction.NORTH) {
				matrix.translate(-1, 0, -1);
			} else if (dir == Direction.EAST) {
				matrix.translate(-1, 0, 0);
			}
		}
		matrix.scale(.45f, .45f, .45f);
		request$drawButtons(entity, matrix.peek(), vertex.getBuffer(RenderLayer.LINES), 1, 1, 1, 1);
		if (entity.rpui.inspectingItem == null) {
			request$renderItems(entity, tickDelta, matrix, vertex, light, overlay);
		} else {
			request$renderInspectedItem(entity, tickDelta, matrix, vertex, light, overlay);
		}
		if (entity.rpui.hasFilter()) {
			matrix.push();
			matrix.translate(2, 2.5, -.009774);
			String text = entity.rpui.filter;
			if (text.length() > 10) text = text.substring(text.length()-10);
			matrix.scale(-.03f, -.03f, 1);
			mc.textRenderer.drawWithShadow(matrix, text, 0, 0, 0xffffffff);
			matrix.pop();
		}
		matrix.pop();
	}
	public void request$renderItems(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		int x = -1;
		int y = 0;
		World world = entity.getWorld();
		ItemRenderer ir = mc.getItemRenderer();
		Quaternion spinQuat = Vec3f.POSITIVE_Y.getDegreesQuaternion(world == null ? 0f : (float) Math.sin((world.getTime() + tickDelta)));
		for (int i=0, size=entity.rpui.items.size(); i<size; i++) {
			Map.Entry<RequestPipeUi.Key, RequestPipeUi.Data> entry = entity.rpui.items.get(i);
			RequestPipeUi.Key key = entry.getKey();
			if (key.item == Items.AIR) continue;
			if (x++>=4) {
				x = 0;
				if (y++ >= 3) break;
			}
			ItemStack stack = key.item.getDefaultStack();
			stack.setNbt(key.nbt);
			matrix.push();
			matrix.translate(x*.45f+.2f, y*.45f+.15f, i == entity.rpui.selectedItem ? .3 : 0);
			RequestPipeUi.Data data = entry.getValue();
			if (data != null && data.reqCount > 0) {
				stack.setCount(data.reqCount);
				matrix.multiply(spinQuat);
			}
			ir.renderItem(null, stack, ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
	}
	public void request$renderInspectedItem(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		World world = entity.getWorld();
		ItemRenderer ir = mc.getItemRenderer();
		TextRenderer tr = mc.textRenderer;
		Map.Entry<RequestPipeUi.Key, RequestPipeUi.Data> entry = entity.rpui.inspectingItem;
		ItemStack stack = entry.getKey().item.getDefaultStack();
		stack.setNbt(entry.getKey().nbt);
		matrix.push();
		matrix.translate(1.1f, 1.85f, 0);
		RequestPipeUi.Data data = entry.getValue();
		if (data != null) {
			matrix.push();
			matrix.scale(-.03f, -.03f, 1);
			tr.drawWithShadow(matrix, intToShortenedString(data.count), 0, 0, 0xffffffff);
			matrix.translate(0, 9, 0);
			tr.drawWithShadow(matrix, intToShortenedString(data.reqCount), 0, 0, 0xffff4010);

			matrix.pop();
		}
		ir.renderItem(null, stack, ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
		matrix.pop();

	}

	public void request$drawButtons(T entity, MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha) {
		drawLine(entry, vertexConsumer, 2.15f, 2, 0, 1.85f, 2.15f, 0, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, 2.15f, 2, 0, 1.85f, 1.85f, 0, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, 2.15f, 2, 0, 1.85f, 2.125f, .025f, 0, 0, 0, 1-red, 1-green, 1-blue, alpha);
		drawLine(entry, vertexConsumer, 2.15f, 2, 0, 1.85f, 1.875f, .025f, 0, 0, 0, 1-red, 1-green, 1-blue, alpha);

		drawLine(entry, vertexConsumer, 2.15f, 2.15f, 0, 1.85f, 2, 0, -1.8, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, 2.15f, 1.85f, 0, 1.85f, 2, 0, -1.8, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, 2.15f, 2.125f, 0, 1.85f, 2, .025f, -1.8, 0, 0, 1-red, 1-green, 1-blue, alpha);
		drawLine(entry, vertexConsumer, 2.15f, 1.875f, 0, 1.85f, 2, .025f, -1.8, 0, 0, 1-red, 1-green, 1-blue, alpha);

		drawLine(entry, vertexConsumer, 2.15f, 2, 0, 2, 1.88f, 0, -1.35, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, 1.8f, 2.15f, 0, 2, 1.88f, 0, -1.35, 0, 0, red, green, blue, alpha);

		drawLine(entry, vertexConsumer, 1.1f, 2.15f, 0, 1.4f, 1.88f, 0, 0.3, 0, 0, red, green, blue, alpha);
		if (!entity.rpui.hasFilter() && entity.rpui.inspectingItem == null) {
			drawLine(entry, vertexConsumer, 1.4f, 2.15f, 0, 1.1f, 1.88f, 0, 0.3, 0, 0, red, green, blue, alpha);
		}
	}
	public static String intToShortenedString(int i) {
		if (i == Integer.MAX_VALUE) return "ALL";
		String ret = Integer.toString(i);
		int len = ret.length();
		switch (len) {
			case 0: case 1: case 2: case 3:
				return ret;
			case 4: case 5: case 6:
				return ret.substring(0, len-3)+"K";
			case 7: case 8: case 9:
				return ret.substring(0, len-6)+"M";
		}
		return "INF";
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
