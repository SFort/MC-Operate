package tf.ssf.sfort.operate.stak;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import tf.ssf.sfort.operate.Config;

import static tf.ssf.sfort.operate.MainClient.mc;

public class BitStakRenderer {
    private int step = 0;

    public static void register() {
        if (Config.fancyInv == Config.EnumOnOffExamine.OFF || BitStakEntity.ENTITY_TYPE == null) return;
        BlockEntityRendererRegistry.register(BitStakEntity.ENTITY_TYPE, ctx -> new BitStakRenderer()::render);
    }

    public static ImmutableMap<Direction, Vec3d> mapTransform;

    static {
        ImmutableMap.Builder<Direction, Vec3d> bldr = new ImmutableMap.Builder<>();
        bldr.put(Direction.EAST, new Vec3d(1.01, 1, 1));
        bldr.put(Direction.SOUTH, new Vec3d(0, 1, 1.01));
        bldr.put(Direction.NORTH, new Vec3d(1, 1, -.01));
        bldr.put(Direction.WEST, new Vec3d(-.01, 1, 0));
        mapTransform = bldr.build();
    }

    public void render(BitStakEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
        BlockState state = entity.getCachedState();
        if (!state.get(BitStak.POWERED)) return;
        if (mc.cameraEntity == null) return;
        Direction dir = mc.cameraEntity.getHorizontalFacing();
        Direction face = state.get(BitStak.FACING);
        if (face.equals(dir)) return;
        matrix.push();
        Vec3d tr = mapTransform.get(face);
        if (tr != null)
            matrix.translate(tr.getX(), tr.getY(), tr.getZ());
        matrix.push();
        matrix.scale(0.015F, -0.015F, 0.015F);

        float rot = face.asRotation();
        if (face.getAxis().equals(Direction.Axis.X))
            rot += 180;
        //matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rot));

        mc.textRenderer.draw("Text", 8, 8, 0xffff4010, true, matrix.peek().getPositionMatrix(), vertex, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        if (!entity.instructions.isEmpty()) {
            mc.getItemRenderer().renderItem(entity.instructions.get(step).getDefaultStack(), ModelTransformationMode.GROUND, 15728880, overlay, matrix, vertex, entity.getWorld(), 1);
            if (step < entity.instructions.size()) {
                step++;
            } else {
                step = 0;
            }
        }
        matrix.scale(10, 10, 10);
        matrix.pop();
        matrix.pop();

    }
}
