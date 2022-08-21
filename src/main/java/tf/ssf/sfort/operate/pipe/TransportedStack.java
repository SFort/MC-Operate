package tf.ssf.sfort.operate.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

public class TransportedStack {
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

	public Direction getPreferredPath(){
		return null;
	}

	public NbtCompound toTag(NbtCompound tag) {
		writeTag(tag);
		return tag;
	}
}
