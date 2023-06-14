package tf.ssf.sfort.operate.cylinder;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import org.joml.Quaternionf;
import tf.ssf.sfort.operate.Config;

import static tf.ssf.sfort.operate.MainClient.mc;

public class CylinderRenderer {
	public static Quaternionf Y_ROT = new Quaternionf();
	public static void register() {
		if (CylinderEntity.ENTITY_TYPE == null) return;
		switch (Config.fancyInv) {
			case ON -> BlockEntityRendererRegistry.register(CylinderEntity.ENTITY_TYPE, ctx -> CylinderRenderer::render);
			case EXAMINE -> BlockEntityRendererRegistry.register(CylinderEntity.ENTITY_TYPE, ctx -> CylinderRenderer::look_render);
		}
	}
	public static void render(CylinderEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		matrix.push();
		matrix.translate(.5, .5, .5);
		if (mc.player != null) {
			matrix.multiply(Y_ROT.setAngleAxis(Math.toRadians(-mc.player.getYaw()), 0, 1, 0));
		}
		matrix.push();
		matrix.translate(0, 0, -.3);
		mc.getItemRenderer().renderItem(entity.nextMissingItem.getDefaultStack(), ModelTransformationMode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, entity.getWorld(), 1);
		matrix.pop();
		matrix.pop();
	}

	public static void look_render(CylinderEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		if (mc.cameraEntity != null && mc.crosshairTarget instanceof BlockHitResult && entity.getPos().equals(((BlockHitResult)mc.crosshairTarget).getBlockPos()))
			render(entity, tickDelta, matrix, vertex, light, overlay);
	}
}
