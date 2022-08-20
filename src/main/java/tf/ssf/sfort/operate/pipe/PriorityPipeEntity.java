package tf.ssf.sfort.operate.pipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class PriorityPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<PriorityPipeEntity> ENTITY_TYPE;
	public byte connectedLowPrioritySides = 0;

	public PriorityPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("priority_pipe"), FabricBlockEntityTypeBuilder.create(PriorityPipeEntity::new, PriorityPipe.BLOCK).build(null));
	}
	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		tag.putByte("priority$lps", connectedLowPrioritySides);
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putByte("priority$lps", connectedLowPrioritySides);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		connectedLowPrioritySides = tag.getByte("priority$lps");
		markDirty();
	}
	@Override
	public void wrenchSideIndirect(Direction side) {}
	@Override
	public void wrenchNeighbour(Direction side){}
	@Override
	public void wrenchSide(Direction side) {
		int bit = 1 << side.ordinal();
		if ((connectedLowPrioritySides & bit) != 0) {
			connectedLowPrioritySides ^= bit;
		} else {
			if ((connectedSides & bit) != 0)
				connectedLowPrioritySides ^= bit;
			connectedSides ^= bit;
		}
		markDirty();
	}
	@Override
	public List<Direction> getOutputs(TransportedStack transport){
		List<Direction> ret = Arrays.stream(Direction.values()).filter(d -> transport.origin != d && (connectedSides & (1 << d.ordinal())) != 0).collect(Collectors.toList());
		Collections.shuffle(ret);
		return ret;
	}
	public List<Direction> getLowPriorityOutputs(TransportedStack transport){
		List<Direction> ret = Arrays.stream(Direction.values()).filter(d -> transport.origin != d && (connectedLowPrioritySides & (1 << d.ordinal())) != 0).collect(Collectors.toList());
		Collections.shuffle(ret);
		return ret;
	}
	@Override
	public long progressQueue() {
		if (world == null) return -1;
		if (itemQueue.isEmpty()) return -1;

		TransportedStack entry = itemQueue.element();
		while (entry.travelTime <= world.getLevelProperties().getTime()) {
			if (!transportStack(entry, getOutputs(entry))) {
				if (!transportStack(entry, getLowPriorityOutputs(entry))) {
					Vec3i dropDir = entry.origin.getOpposite().getVector();
					ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5 + (dropDir.getX() >> 1), pos.getY() + .5 + (dropDir.getY() >> 1), pos.getZ() + .5 + (dropDir.getZ() >> 1), entry.stack);
					itemEntity.addVelocity(dropDir.getX(), dropDir.getY(), dropDir.getZ());
					world.spawnEntity(itemEntity);
				}
			}
			itemQueue.poll();
			markDirty();
			if (itemQueue.isEmpty()) {
				return -1;
			} else {
				entry = itemQueue.element();
			}
		}
		return entry.travelTime;
	}
	@Override
	@Environment(EnvType.CLIENT)
	public AbstractPipeRenderer.DisconnectedSideLinesRenderer getDisconnectedSideLinesRenderer(Direction dir) {
		int mask = 1 << dir.ordinal();
		return (connectedSides & mask) == 0 && (connectedLowPrioritySides & mask) == 0 ? AbstractPipeRenderer::drawDisconnectedSideLines : null;
	}
	@Override
	public Block asBlock() {
		return PriorityPipe.BLOCK;
	}
}
