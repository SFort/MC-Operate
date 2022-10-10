package tf.ssf.sfort.operate.pipe.advanced.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class PipePathing {
	public static final PipePathing NULL = new PipePathing((Entry) null) {
		public Direction next() {
			return null;
		}
	};
	public Entry first;
	public PipePathing(NbtCompound tag) {
		if (tag == null) return;
		int i=1;
		Entry last;
		if (tag.contains("0", NbtElement.BYTE_TYPE)) {
			Entry dir = new Entry(Direction.byId(tag.getByte("0")));
			last = first = dir;
		} else return;
		String key = "1";
		while (tag.contains(key, NbtElement.BYTE_TYPE)) {
			Entry dir = new Entry(Direction.byId(tag.getByte(key)));
			last = last.next =  dir;
			key = Integer.toString(++i);
		}
	}
	public PipePathing(Entry first) {
		this.first = first;
	}
	public Direction next() {
		if (first == null) return null;
		Direction ret = first.direction;
		first = first.next;
		return ret;
	}

	public NbtCompound toNbt() {
		NbtCompound tag = new NbtCompound();
		int i=0;
		for (Entry entry = first; entry != null; entry=entry.next) {
			tag.putByte(Integer.toString(i++), (byte) entry.direction.getId());
		}
		return tag;
	}

	public static class Entry {
		public Entry next;
		public Direction direction;

		public Entry(Direction direction) {
			this.direction = direction;
		}
		public Entry(Direction direction, Entry next) {
			this.direction = direction;
			this.next = next;
		}
		public void writeNbt(String key, NbtCompound tag) {
			if (direction == null) return;
			tag.putByte(key, (byte) direction.getId());
		}
	}
	public static class Builder {
		Map<BlockPos, Entry> map = new HashMap<>();

		public void init(BlockPos pos, Direction dir) {
			map.put(pos, new Entry(dir));
		}

		public void attach(BlockPos attachTo, BlockPos pos, Direction dir) {
			Entry entry = map.get(attachTo);
			if (entry == null) return;
			Entry n = new Entry(dir, entry);
			map.put(pos, n);
		}

		public ImmutableMap<BlockPos, Entry> build() {
			return ImmutableMap.copyOf(map);
		}
	}
}
