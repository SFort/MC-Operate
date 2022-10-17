package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.util.TransportedStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


public class PriorityPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<PriorityPipeEntity> ENTITY_TYPE;
	public byte connectedLowPrioritySidesByte = 0;
	public Direction[] connectedLowPrioritySides = new Direction[0];

	public PriorityPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("priority_pipe"), FabricBlockEntityTypeBuilder.create(PriorityPipeEntity::new, PriorityPipe.BLOCK).build(null));
	}
	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		tag.putByte("priority$lps", connectedLowPrioritySidesByte);
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putByte("priority$lps", connectedLowPrioritySidesByte);
	}

	@Override
	public void readNbtCommon(NbtCompound tag) {
		super.readNbtCommon(tag);
		connectedLowPrioritySidesByte = tag.getByte("priority$lps");
		Direction[] readingSides = new Direction[Integer.bitCount(connectedLowPrioritySidesByte & 0b111111)];
		int i = 0;
		for (Direction d : Direction.values()) {
			if ((connectedLowPrioritySidesByte & (1 << d.ordinal())) != 0) readingSides[i++] = d;
		}
		connectedLowPrioritySides = readingSides;
	}
	@Override
	public void wrenchSideIndirect(Direction side) {}
	@Override
	public void wrenchNeighbour(Direction side){}
	@Override
	public void wrenchSide(Direction side) {
		markDirty();
		int bit = 1 << side.ordinal();
		if ((connectedLowPrioritySidesByte & bit) != 0) {
			priority$toggleLowPriorityConnection(side);
		} else {
			if ((connectedSidesByte & bit) != 0)
				priority$toggleLowPriorityConnection(side);
			super.toggleConnection(side);
		}
	}
	public void priority$toggleLowPriorityConnection(Direction side) {
		connectedLowPrioritySidesByte ^= 1 << side.ordinal();
		for (int i=0;i<connectedLowPrioritySides.length;i++) {
			if (connectedLowPrioritySides[i] != side) continue;
			Direction[] n = new Direction[connectedLowPrioritySides.length-1];
			System.arraycopy(connectedLowPrioritySides, 0, n, 0, i);
			if (i < n.length) System.arraycopy(connectedLowPrioritySides, i+1, n, i, connectedLowPrioritySides.length-i-1);
			connectedLowPrioritySides = n;
			return;
		}
		Direction[] n = new Direction[connectedLowPrioritySides.length+1];
		System.arraycopy(connectedLowPrioritySides, 0, n, 0, connectedLowPrioritySides.length);
		n[connectedLowPrioritySides.length] = side;
		connectedLowPrioritySides = n;
	}
	@Override
	public Function<TransportedStack, List<Direction>> getOutputs(){
		AtomicReference<Direction> lastDir = new AtomicReference<>();
		List<Direction> ret = new ArrayList<>();
		List<Direction> retLow = new ArrayList<>();
		return stack -> {
			if (lastDir.get() != stack.origin) {
				lastDir.set(stack.origin);
				ret.clear();
				retLow.clear();
				for (Direction d : connectedSides) {
					if (stack.origin != d) ret.add(d);
				}
				for (Direction d : connectedLowPrioritySides) {
					if (stack.origin != d) retLow.add(d);
				}
			}
			Collections.shuffle(ret);
			Collections.shuffle(retLow);
			List<Direction> r = new ArrayList<>();
			r.addAll(ret);
			r.addAll(retLow);
			return r;
		};
	}

	@Override
	public boolean isConnected(Direction dir) {
		int mask = 1 << dir.ordinal();
		return (connectedSidesByte & mask) != 0 || (connectedLowPrioritySidesByte & mask) != 0;
	}
	@Override
	public Block asBlock() {
		return PriorityPipe.BLOCK;
	}
}
