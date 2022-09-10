package tf.ssf.sfort.operate.punch;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import tf.ssf.sfort.operate.Config;

import java.util.function.Consumer;

import static tf.ssf.sfort.operate.client.McClient.mc;

public class PunchRenderer {
    public static void register() {
        if (Config.fancyInv == null || Config.punch == null) return;
        BlockEntityRendererRegistry.register(PunchEntity.ENTITY_TYPE, Config.fancyInv ? ctx -> PunchRenderer::render : ctx -> PunchRenderer::look_render);
    }

    public static void render(PunchEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
        if (!(entity.craftResultDisplay.isEmpty() || entity.getCachedState().get(Punch.POWERED))) {
            matrix.push();
            matrix.translate(0.5, 1, 0.5);
            mc.getItemRenderer().renderItem(entity.craftResultDisplay, ModelTransformation.Mode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, 1);
            matrix.pop();
        }
        renderSide(entity, tickDelta, matrix, vertex, light, overlay, m -> {
        });
        renderSide(entity, tickDelta, matrix, vertex, light, overlay,
                m -> {
                    m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
                    m.translate(-1, 0, 0);
                });
        renderSide(entity, tickDelta, matrix, vertex, light, overlay,
                m -> {
                    m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270));
                    m.translate(0, 0, -1);
                });
        renderSide(entity, tickDelta, matrix, vertex, light, overlay,
                m -> {
                    m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                    m.translate(-1, 0, -1);
                });
    }

    public static void renderSide(PunchEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay, Consumer<MatrixStack> applyDirection) {
        for (int i = 0; i < entity.inv.inv.length; i++) {
            ItemStack item = entity.inv.inv[i];
            matrix.push();
            applyDirection.accept(matrix);
            matrix.translate(0.28 + (i % 3) * 0.22, 0.61 - (i / 3) * 0.22, 0.95 + 0.01*i);
            matrix.push();
            matrix.scale(0.6f, 0.6f, 0.6f);
            if (!item.isEmpty())
                mc.getItemRenderer().renderItem(item, ModelTransformation.Mode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, 1);
            matrix.pop();
            matrix.pop();
        }
    }

    public static void look_render(PunchEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
        Vec3d pos = mc.cameraEntity.raycast(8, tickDelta, false).getPos();
        if (mc.cameraEntity != null)
            if (pos.squaredDistanceTo(entity.getPos().getX() + 0.5, entity.getPos().getY() + 1, entity.getPos().getZ() + 0.5) < 0.6 || pos.squaredDistanceTo(entity.getPos().getX() + 0.5, entity.getPos().getY() + 0.5, entity.getPos().getZ() + 0.5) < 0.6) {
                int dir = mc.cameraEntity.getHorizontalFacing().getId();
                if (dir > 1) {
                    if (!(entity.craftResultDisplay.isEmpty() || entity.getCachedState().get(Punch.POWERED))) {
                        matrix.push();
                        matrix.translate(0.5, 1, 0.5);
                        mc.getItemRenderer().renderItem(entity.craftResultDisplay, ModelTransformation.Mode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, 1);
                        matrix.pop();
                    }
                    final Consumer<MatrixStack> rot = switch (dir) {
                        case 3 -> m -> {
                            m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                            m.translate(-1, 0, -1);
                        };
                        case 4 -> m -> {
                            m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
                            m.translate(-1, 0, 0);
                        };
                        case 5 -> m -> {
                            m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270));
                            m.translate(0, 0, -1);
                        };
                        default -> m -> {
                        };
                    };
                    renderSide(entity, tickDelta, matrix, vertex, light, overlay, rot);

                } else {
                    render(entity, tickDelta, matrix, vertex, light, overlay);
                }
            }
    }
}
