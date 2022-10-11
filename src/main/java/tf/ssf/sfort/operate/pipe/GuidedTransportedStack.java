package tf.ssf.sfort.operate.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import tf.ssf.sfort.operate.pipe.advanced.util.PipePathing;

import java.util.List;

public class GuidedTransportedStack extends TransportedStack {
	public final PipePathing path;
	public GuidedTransportedStack(NbtCompound tag) {
		super(tag);
		PipePathing path = new PipePathing(tag.getCompound("guided"));
		this.path = path.first == null ? PipePathing.NULL : path;
	}

	public GuidedTransportedStack(ItemStack stack, Direction dir, int i, PipePathing path) {
		super(stack, dir, i);
		this.path = path;
	}

	/*@Override
	public void writeClientTag(NbtCompound tag) {
		super.writeClientTag(tag);
		tag.putString("super", "guided");
		if (path != PipePathing.NULL && path.first != null) {
			NbtCompound pathTag = new NbtCompound();
			pathTag.putByte("0", (byte) path.first.direction.getId());
			tag.put("guided", pathTag);
		}
	}*/

	@Override
	public void writeTag(NbtCompound tag) {
		super.writeTag(tag);
		tag.putString("super", "guided");
		if (path != PipePathing.NULL && path.first != null) {
			tag.put("guided", this.path.toNbt());
		}
	}

	@Override
	public Direction getPreferredPath(List<Direction> outputs, BlockPos pos){
		return path.next();
	}
}
