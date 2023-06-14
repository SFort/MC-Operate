package tf.ssf.sfort.operate.jolt;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import tf.ssf.sfort.operate.Config;

import static tf.ssf.sfort.operate.MainClient.mc;

public class JoltRenderer {
    public static void register() {
        if (JoltEntity.ENTITY_TYPE == null) return;
        switch (Config.fancyInv) {
            case ON -> BlockEntityRendererRegistry.register(JoltEntity.ENTITY_TYPE, ctx -> JoltRenderer::render);
            case EXAMINE -> BlockEntityRendererRegistry.register(JoltEntity.ENTITY_TYPE, ctx -> JoltRenderer::look_render);
        }
    }

    public static void render(JoltEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
        matrix.push();
        matrix.translate(0.5, 0.75, 0.5);
        mc.getItemRenderer().renderItem(entity.inv, ModelTransformationMode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, entity.getWorld(), 1);
        matrix.pop();
    }

    public static void look_render(JoltEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
        if (mc.cameraEntity != null && mc.cameraEntity.raycast(8, tickDelta, false).getPos().squaredDistanceTo(entity.getPos().getX() + 0.5, entity.getPos().getY() + 1, entity.getPos().getZ() + 0.5) < 0.6)
            render(entity, tickDelta, matrix, vertex, light, overlay);
    }
}
