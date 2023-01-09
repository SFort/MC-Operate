package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import tf.ssf.sfort.operate.Config;

import static tf.ssf.sfort.operate.MainClient.mc;

public class FilterPipeRenderer<T extends FilterPipeEntity> extends AbstractPipeRenderer<T> {
	//Vec3f.POSITIVE_X.getDegreesQuaternion(90)
	public static final Quaternionf Y_ROTATION = new Quaternionf();
	//Vec3f.POSITIVE_Y.getDegreesQuaternion(90)
	public static final Quaternionf X_ROTATION = new Quaternionf();

	static {
		Y_ROTATION.setAngleAxis(Math.toRadians(90), 1, 0, 0);
		X_ROTATION.setAngleAxis(Math.toRadians(90), 0, 1, 0);
	}

	public static void register() {
		if (Config.basicPipe == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BlockEntityRendererRegistry.register(FilterPipeEntity.ENTITY_TYPE, ctx -> new FilterPipeRenderer<>()::render);
	}
	@Override
	public void render(T entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		super.render(entity, tickDelta, matrix, vertex, light, overlay);
		ItemRenderer ir = mc.getItemRenderer();
		matrix.push();
		matrix.scale(.5f, .5f, .5f);
		World world = entity.getWorld();
		Item[] filter = entity.filterOutSides;
		if (filter[0] != Items.AIR) {
			matrix.push();
			matrix.multiply(Y_ROTATION);
			matrix.translate(1, .6, -.5);
			ir.renderItem(null, filter[0].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[1] != Items.AIR) {
			matrix.push();
			matrix.multiply(Y_ROTATION);
			matrix.translate(1, 1.18, -1.5);
			ir.renderItem(null, filter[1].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[2] != Items.AIR) {
			matrix.push();
			matrix.translate(1.25, .85, .5);
			ir.renderItem(null, filter[2].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[3] != Items.AIR) {
			matrix.push();
			matrix.translate(.75, .85, 1.5);
			ir.renderItem(null, filter[3].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[4] != Items.AIR) {
			matrix.push();
			matrix.multiply(X_ROTATION);
			matrix.translate(-1.25, .85, .5);
			ir.renderItem(null, filter[4].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[5] != Items.AIR) {
			matrix.push();
			matrix.multiply(X_ROTATION);
			matrix.translate(-.75, .85, 1.5);
			ir.renderItem(null, filter[5].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		//1.18
		filter = entity.filterInSides;
		if (filter[0] != Items.AIR) {
			matrix.push();
			matrix.multiply(Y_ROTATION);
			matrix.translate(1, 1.18, -.5);
			ir.renderItem(null, filter[0].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[1] != Items.AIR) {
			matrix.push();
			matrix.multiply(Y_ROTATION);
			matrix.translate(1, .6, -1.5);
			ir.renderItem(null, filter[1].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[2] != Items.AIR) {
			matrix.push();
			matrix.translate(.75, .85, .5);
			ir.renderItem(null, filter[2].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[3] != Items.AIR) {
			matrix.push();
			matrix.translate(1.25, .85, 1.5);
			ir.renderItem(null, filter[3].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[4] != Items.AIR) {
			matrix.push();
			matrix.multiply(X_ROTATION);
			matrix.translate(-.75, .85, .5);
			ir.renderItem(null, filter[4].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		if (filter[5] != Items.AIR) {
			matrix.push();
			matrix.multiply(X_ROTATION);
			matrix.translate(-1.25, .85, 1.5);
			ir.renderItem(null, filter[5].getDefaultStack(), ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
			matrix.pop();
		}
		matrix.pop();
	}
}
