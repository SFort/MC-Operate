package tf.ssf.sfort.operate.pipe;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TransportedStack {
	public static final Quaternion X_ROTATION = Vec3f.POSITIVE_Y.getDegreesQuaternion(90);
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

	public TransportedStack(ItemStack stack, Direction origin, long travelTime) {
		this.stack = stack;
		this.origin = origin;
		this.travelTime = travelTime;
	}

	public TransportedStack(NbtCompound tag) {
		this.stack = ItemStack.fromNbt(tag.getCompound("stack"));
		this.origin = Direction.values()[Math.min(5, Math.max(0, tag.getInt("origin")))];
		this.travelTime = tag.getLong("ttime");
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

	public void render(double progress, World world, ItemRenderer ir, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		matrix.push();
		switch (this.origin) {
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
		ir.renderItem(null, this.stack, ModelTransformation.Mode.GROUND, false, matrix, vertex, world, light, overlay, -1);
		matrix.pop();
		matrix.pop();
	}
}
