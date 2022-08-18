package tf.ssf.sfort.operate.tube;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import tf.ssf.sfort.operate.Config;

public class ColorTubeRenderer {

	public static void register() {
		if (Config.colorTube == null) return;
		BlockEntityRendererRegistry.register(ColorTubeEntity.ENTITY_TYPE, ctx -> new ColorTubeRenderer()::render);
	}

	public void render(ColorTubeEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		for (int i = 0; i < entity.sides.length; i++) {
			TubeConnectTypes side = entity.sides[i];
			matrix.push();
			matrix.translate(.5, .5, .5);
			matrix.push();
			Direction dir = Direction.values()[i];
			matrix.multiply(dir.getRotationQuaternion());
			switch (dir) {
				case DOWN -> matrix.scale(1, 1, -1);
				case WEST, NORTH -> matrix.scale(-1, 1, 1);
			}
			if (side == TubeConnectTypes.ALL) {
				drawSideLines(matrix.peek(), vertex.getBuffer(RenderLayer.LINES), 1, 1, 1, .7f);
				drawColorLines(matrix, vertex.getBuffer(RenderLayer.LINES), entity.colorLvl);
			} else if (side.color != null) {
				drawSideFocus(matrix.peek(), vertex.getBuffer(RenderLayer.LINES), side.red, side.green, side.blue, 1);
				drawStrengthLine(matrix.peek(), vertex.getBuffer(RenderLayer.LINES), side, side.getStrength(entity.colorLvl));
			}
			matrix.pop();
			matrix.pop();
		}
	}

	public static void drawColorLines(MatrixStack matrix, VertexConsumer vertexConsumer, long clr) {
		if (clr == 0) return;
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.WHITE, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.ORANGE, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.MAGENTA, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.LIGHT_BLUE, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.YELLOW, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.LIME, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.PINK, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.GREY, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.LIGHT_GREY, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.CYAN, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.PURPLE, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.BLUE, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.BROWN, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.GREEN, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.RED, clr);
		drawColorLine(matrix, vertexConsumer, TubeConnectTypes.BLACK, clr);
	}

	public static void drawColorLine(MatrixStack matrix, VertexConsumer vertexConsumer, TubeConnectTypes connection, long clr) {
		int strength = connection.getStrength(clr);
		if (strength > 0)
			drawLine(matrix.peek(), vertexConsumer, connection.renderX, .25f, connection.renderZ, connection.renderX, .5f, connection.renderZ, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
	}

	public static void drawStrengthLine(MatrixStack.Entry entry, VertexConsumer vertexConsumer, TubeConnectTypes connection, int strength) {
		switch (strength) {
			case 15:
				drawLine(entry, vertexConsumer, .1f, .25f, .1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 14:
				drawLine(entry, vertexConsumer, .05f, .25f, .1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 13:
				drawLine(entry, vertexConsumer, 0, .25f, .1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 12:
				drawLine(entry, vertexConsumer, -.05f, .25f, .1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 11:
				drawLine(entry, vertexConsumer, -.1f, .25f, .1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 10:
				drawLine(entry, vertexConsumer, .1f, .25f, 0, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 9:
				drawLine(entry, vertexConsumer, .05f, .25f, 0, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 8:
				drawLine(entry, vertexConsumer, 0, .25f, 0, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 7:
				drawLine(entry, vertexConsumer, -.05f, .25f, 0, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 6:
				drawLine(entry, vertexConsumer, -.1f, .25f, 0, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 5:
				drawLine(entry, vertexConsumer, .1f, .25f, -.1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 4:
				drawLine(entry, vertexConsumer, .05f, .25f, -.1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 3:
				drawLine(entry, vertexConsumer, 0, .25f, -.1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 2:
				drawLine(entry, vertexConsumer, -.05f, .25f, -.1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);
			case 1:
				drawLine(entry, vertexConsumer, -.1f, .25f, -.1f, 0, .5f, 0, 0, 0, 0, connection.red, connection.green, connection.blue, 1);

		}
	}

	public static void drawSideFocus(MatrixStack.Entry entry, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha) {
		drawLine(entry, vertexConsumer, -.125f, .25f, -.125f, 0, .5f, 0, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .25f, -.125f, 0, .5f, 0, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, -.125f, .25f, .125f, 0, .5f, 0, 0, 0, 0, red, green, blue, alpha);
		drawLine(entry, vertexConsumer, .125f, .25f, .125f, 0, .5f, 0, 0, 0, 0, red, green, blue, alpha);
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
