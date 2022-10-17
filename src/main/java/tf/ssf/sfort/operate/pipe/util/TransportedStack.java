package tf.ssf.sfort.operate.pipe.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TransportedStack {
	public static final Quaternion X_ROTATION = Vec3f.POSITIVE_Y.getDegreesQuaternion(90);
	public static final Quaternion X_FLIP_ROTATION = Vec3f.POSITIVE_Y.getDegreesQuaternion(180);
	public static final ItemStack bundleStack = Items.BUNDLE.getDefaultStack();
	public static final Map<String, Function<NbtCompound, TransportedStack>> superNbtConstructors = new HashMap<>();
	static {
		superNbtConstructors.put("guided", GuidedTransportedStack::new);
	}

	public static TransportedStack fromNbt(NbtCompound tag) {
		if (tag.contains("super", NbtElement.STRING_TYPE)) {
			Function<NbtCompound, TransportedStack> s = superNbtConstructors.get(tag.getString("super"));
			if (s != null) {
				TransportedStack ret = s.apply(tag);
				if (ret != null) return ret;
			}
		}
		return new TransportedStack(tag);
	}

	public final ItemStack stack;
	public Direction origin;
	public long travelTime;
	public boolean drawSkipped = false;

	public TransportedStack(ItemStack stack, Direction origin, long travelTime) {
		this.stack = stack;
		this.origin = origin;
		this.travelTime = travelTime;
	}

	public TransportedStack(NbtCompound tag) {
		this.stack = ItemStack.fromNbt(tag.getCompound("stack"));
		this.origin = Direction.values()[Math.min(5, Math.max(0, tag.getInt("origin")))];
		this.travelTime = tag.getLong("ttime");

		NbtCompound client = tag.getCompound("client");
		if (!client.isEmpty()) {
			this.drawSkipped = client.getBoolean("skipped");
		}
	}

	public void writeTag(NbtCompound tag) {
		tag.put("stack", this.stack.writeNbt(new NbtCompound()));
		tag.putInt("origin", this.origin.ordinal());
		tag.putLong("ttime", this.travelTime);
	}

	public void writeClientTag(NbtCompound tag) {
		tag.put("stack", this.stack.writeNbt(new NbtCompound()));
		tag.putInt("origin", this.origin.ordinal());
		tag.putLong("ttime", this.travelTime);
	}

	public Direction getPreferredPath(List<Direction> outputs, BlockPos pos){
		return null;
	}

	public NbtCompound toTag(NbtCompound tag) {
		writeTag(tag);
		return tag;
	}

	public NbtCompound toClientTag(NbtCompound tag) {
		writeClientTag(tag);
		return tag;
	}

	public NbtCompound toClientTag() {
		return toClientTag(new NbtCompound());
	}

	public void render(double progress, World world, ItemRenderer ir, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay, boolean xFlip, boolean zFlip, boolean xMid, boolean zMid) {
		switch (this.origin) {
			case UP:
				matrix.translate(.5, .5 + progress, .37);
				if (zFlip) matrix.multiply(X_FLIP_ROTATION);
				break;
			case DOWN:
				matrix.translate(.5, .5 - progress, .63);
				if (zMid) matrix.multiply(X_FLIP_ROTATION);
				break;
			case NORTH:
				matrix.translate(.37, .45, .5 - progress);
				matrix.multiply(X_ROTATION);
				if (xFlip) matrix.multiply(X_FLIP_ROTATION);
				break;
			case SOUTH:
				matrix.translate(.63, .45, .5 + progress);
				matrix.multiply(X_ROTATION);
				if (xMid) matrix.multiply(X_FLIP_ROTATION);
				break;
			case WEST:
				matrix.translate(.5 - progress, .45, .37);
				if (zMid) matrix.multiply(X_FLIP_ROTATION);
				break;
			case EAST:
				matrix.translate(.5 + progress, .45, .63);
				if (zFlip) matrix.multiply(X_FLIP_ROTATION);
		}
		matrix.push();
		if (this.drawSkipped) {
			matrix.push();
			matrix.scale(.3f, .3f, .3f);
			matrix.translate(0, 0, 0.01);
			renderStack(bundleStack, world, ir, tickDelta, matrix, vertex, light, overlay);
			matrix.pop();
			matrix.translate(0, .1, 0);
			matrix.scale(.15f, .15f, .15f);
			renderStack(this.stack, world, ir, tickDelta, matrix, vertex, light, overlay);
		} else {
			matrix.scale(.5f, .5f, .5f);
			renderStack(this.stack, world, ir, tickDelta, matrix, vertex, light, overlay);
		}
		matrix.pop();
	}

	public static void renderStack(ItemStack stack, World world, ItemRenderer ir, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		if (stack.isEmpty()) return;
		BakedModel bakedModel = ir.getModel(stack, world, null, -1);
		Sprite sprite = bakedModel.getParticleSprite();
		if (sprite == null) return;
		float l = sprite.getMinU();
		float m = sprite.getMaxU();
		float n = sprite.getMinV();
		float o = sprite.getMaxV();
		// POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
		RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
		VertexConsumer vertexConsumer = vertex.getBuffer(TexturedRenderLayers.getEntityTranslucentCull());
		Matrix4f mat = matrix.peek().getPositionMatrix();
		vertexConsumer.vertex(mat,-0.25f, -0.15f, 0.0f).color(255, 255, 255, 255).texture(m, o).overlay(overlay).light(light).normal(0, 0, 0).next();
		vertexConsumer.vertex(mat,-0.25f, 0.35f, 0.0f).color(255, 255, 255, 255).texture(m, n).overlay(overlay).light(light).normal(0, 0, 0).next();
		vertexConsumer.vertex(mat,0.25f, 0.35f, 0.0f).color(255, 255, 255, 255).texture(l, n).overlay(overlay).light(light).normal(0, 0, 0).next();
		vertexConsumer.vertex(mat,0.25f, -0.15f, 0.0f).color(255, 255, 255, 255).texture(l, o).overlay(overlay).light(light).normal(0, 0, 0).next();

	}
}
