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
	public InvNode first;
	public InvNode last;
	public ImmutableMap<BlockPos, PipePathing.Entry> pathing;

	public boolean firstEntry(World world, Predicate<InvNode> scanner) {
		InvNode invNode = first;
		InvNode prev = null;
		while (invNode != null) {
			if (invNode.pos == null) {
				invNode = remove(invNode, prev);
				continue;
			}
			if (invNode.dir == null) {
				invNode.updateFacing(world);
				if (invNode.dir == null) {
					invNode = remove(invNode, prev);
					continue;
				}
			}
			if (invNode.getInv() != null || invNode.updateInv(world)) {
				if (scanner.test(invNode)) return false;
			}
			prev = invNode;
			invNode = invNode.next;
		}
		return true;
	}

	public InvNode remove(InvNode invNode, InvNode previous) {
		if (invNode == null) return null;
		if (invNode == first){
			first = first.next;
			if (first == null) {
				last = null;
			}
			return first;
		}
		if (previous == null) return null;
		if (invNode == last) {
			previous.next = null;
			last = previous;
			return last;
		}
		previous.next = invNode.next;
		return invNode.next;
	}
	public void push(BlockPos pos) {
		push(pos, null);
	}
	public void push(BlockPos pos, Direction dir) {
		if (pos == null) return;
		if (last != null) {
			last.next = new InvNode(pos, dir);
			last = last.next;
		} else first = last = new InvNode(pos, dir);
	}

	public static class InvNode {
		public final BlockPos pos;
		public Direction dir;
		public WeakReference<Inventory> inv;
		public InvNode next = null;
		public InvNode(BlockPos pos, Direction dir) {
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
		public void updateFacing(World world) {
			BlockState state = world.getBlockState(pos);
			if (state.isOf(OverseerPipe.BLOCK)) {
				dir = state.get(OverseerPipe.FACING);
			}
		}
		public boolean updateInv(World world) {
			BlockEntity be = world.getBlockEntity(getTargeting());
			if (be instanceof Inventory) {
				inv = new WeakReference<>((Inventory)be);
				return true;
			}
			return false;
		}
		public Inventory getInv(World world) {
			if (dir == null) {
				updateFacing(world);
			}
			if (getInv() != null || updateInv(world)) {
					return getInv();
			}
			return null;
		}
	}

}
