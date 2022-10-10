package tf.ssf.sfort.operate.pipe.advanced.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.pipe.advanced.OverseerPipe;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

public class RequestPipeCache {
	public Entry first;
	public Entry last;
	public ImmutableMap<BlockPos, PipePathing.Entry> pathing;

	public int firstEntry(World world, Predicate<Entry> scanner) {
		Entry entry = first;
		Entry prev = null;
		int dirty = 0;
		while (entry != null) {
			if (entry.pos == null) {
				dirty = 1;
				entry = remove(entry, prev);
				continue;
			}
			if (entry.dir == null) {
				dirty = 1;
				BlockState state = world.getBlockState(entry.pos);
				if (state.isOf(OverseerPipe.BLOCK)) {
					entry.dir = state.get(OverseerPipe.FACING);
				} else {
					entry = remove(entry, prev);
					continue;
				}
			}
			if (entry.getInv() != null && scanner.test(entry)) {
				return dirty;
			}
			BlockEntity be = world.getBlockEntity(entry.getTargeting());
			if (be instanceof Inventory) {
				entry.inv = new WeakReference<>((Inventory)be);
				if (scanner.test(entry)) return dirty;
			}
			prev = entry;
			entry = entry.next;
		}
		return 2 | dirty;
	}
	public Entry remove(Entry entry, Entry previous) {
		if (entry == null) return null;
		if (entry == first){
			first = first.next;
			if (first == null) {
				last = null;
			}
			return first;
		}
		if (previous == null) return null;
		if (entry == last) {
			previous.next = null;
			last = previous;
			return last;
		}
		previous.next = entry.next;
		return entry.next;
	}
	public void push(BlockPos pos) {
		push(pos, null);
	}
	public void push(BlockPos pos, Direction dir) {
		if (pos == null) return;
		if (last != null) {
			last.next = new Entry(pos, dir);
			last = last.next;
		} else first = last = new Entry(pos, dir);
	}

	public static class Entry {
		public final BlockPos pos;
		public Direction dir;
		public WeakReference<Inventory> inv;
		public Entry next = null;
		public Entry(BlockPos pos, Direction dir) {
			this.pos = pos;
			this.dir = dir;
		}
		public BlockPos getTargeting(){
			return pos.offset(dir);
		}
		public Inventory getInv() {
			if (inv == null) return null;
			return inv.get();
		}
	}

}
