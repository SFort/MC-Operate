package tf.ssf.sfort.operate.pipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.tick.OrderedTick;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public abstract class AbstractPipeEntity extends BlockEntity implements ItemPipeAcceptor {
	public byte connectedSides = 0;

	public final LinkedList<TransportedStack> itemQueue = new LinkedList<>();

	public AbstractPipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(
				this,
				b -> toInitialChunkDataNbt()
		);
	}

	@Override
	public void markDirty() {
		super.markDirty();

		if (world != null && !world.isClient()) {
			((ServerWorld) world).getChunkManager().markForUpdate(getPos());
		}
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = new NbtCompound();
		tag.putByte("sides", connectedSides);
		NbtCompound items = new NbtCompound();
		int i=0;
		for (TransportedStack stack : itemQueue) {
			items.put(Integer.toString(i++), stack.toTag(new NbtCompound()));
		}
		tag.put("items", items);
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putByte("sides", connectedSides);
		NbtCompound items = new NbtCompound();
		int i=0;
		for (TransportedStack stack : itemQueue) {
			items.put(Integer.toString(i++), stack.toTag(new NbtCompound()));
		}
		tag.put("items", items);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		connectedSides = tag.getByte("sides");
		itemQueue.clear();
		NbtCompound items = tag.getCompound("items");
		int i=0;
		while (true) {
			NbtCompound item = items.getCompound(Integer.toString(i++));
			if (item.isEmpty()) break;
			itemQueue.offer(new TransportedStack(item));
		}
		markDirty();
	}
	public void wrenchSideIndirect(Direction side) {
		wrenchSide(side);
	}
	public void wrenchNeighbour(Direction side){
		BlockEntity e = world.getBlockEntity(pos.offset(side));
		if (e instanceof AbstractPipeEntity) {
			((AbstractPipeEntity)e).wrenchSideIndirect(side.getOpposite());
		}
	}
	public void wrenchSide(Direction side) {
		connectedSides ^= 1 << side.ordinal();
		markDirty();
	}
	public List<Direction> getOutputs(TransportedStack transport){
		List<Direction> ret = Arrays.stream(Direction.values()).filter(d -> transport.origin != d && (connectedSides & (1 << d.ordinal())) != 0).collect(Collectors.toList());
		Collections.shuffle(ret);
		return ret;
	}

	public void pipeTick() {
		if (world == null) return;
		if (world instanceof ServerWorld && Config.chunkLoadPipes) {
			((ServerWorld)world).getChunkManager().addTicket(Main.PIPE_TICKET_TYPE, new ChunkPos(pos), 3, pos);
		}
		long nextTransfer = progressQueue();
		if (nextTransfer >= 0) {
			world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, nextTransfer + 1, world.getTickOrder()));
		}
	}
	public long progressQueue() {
		if (world == null) return -1;
		if (itemQueue.isEmpty()) return -1;

		TransportedStack entry = itemQueue.element();
		while (entry.travelTime <= world.getLevelProperties().getTime()) {
			transport :{
				List<Direction> outputs = getOutputs(entry);
				Direction preferredPath = entry.getPreferredPath();
				if (preferredPath != null && outputs.contains(preferredPath) && transportStack(entry, preferredPath))
					break transport;
				if (transportStack(entry, outputs))
					break transport;

				Vec3i dropDir = entry.origin.getOpposite().getVector();
				ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5 + (dropDir.getX() >> 1), pos.getY() + .5 + (dropDir.getY() >> 1), pos.getZ() + .5 + (dropDir.getZ() >> 1), entry.stack);
				itemEntity.addVelocity(dropDir.getX(), dropDir.getY(), dropDir.getZ());
				world.spawnEntity(itemEntity);
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
	public boolean transportStack(TransportedStack entry, List<Direction> sides) {
		for (Direction dir : sides) {
			if (transportStack(entry, dir)) return true;
		}
		return false;
	}
	public boolean transportStack(TransportedStack entry, Direction dir) {
		BlockPos offset = pos.offset(dir);
		if (world.getBlockState(offset).getCollisionShape(world, offset).isEmpty()) {
			Vec3i dropDir = dir.getVector();
			ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5 + (dropDir.getX() >> 1), pos.getY() + .5 + (dropDir.getY() >> 1), pos.getZ() + .5 + (dropDir.getZ() >> 1), entry.stack);
			itemEntity.addVelocity(dropDir.getX(), dropDir.getY(), dropDir.getZ());
			world.spawnEntity(itemEntity);
			return true;
		}
		BlockEntity e = world.getBlockEntity(offset);
		if (e instanceof ItemPipeAcceptor && ((ItemPipeAcceptor) e).acceptItemFrom(entry, dir.getOpposite())) {
			return true;
		}
		if (e instanceof Inventory) {
			int maxInvPerSlot = ((Inventory) e).getMaxCountPerStack();
			IntFunction<Integer> ordinalToSlot = i -> i;
			int loopSize = ((Inventory) e).size();
			if (e instanceof SidedInventory) {
				final int[] availableSlots = ((SidedInventory) e).getAvailableSlots(dir.getOpposite());
				loopSize = availableSlots.length;
				if (loopSize > 0) {
					ordinalToSlot = i -> availableSlots[i];
				}
			}
			ItemStack stack = entry.stack;
			for (int in = 0, i = ordinalToSlot.apply(in); in < loopSize; i = ordinalToSlot.apply(++in)) {
				if (!((Inventory) e).isValid(i, stack)) continue;
				if (e instanceof SidedInventory && !((SidedInventory) e).canInsert(i, stack, dir.getOpposite()))
					continue;
				ItemStack existingStack = ((Inventory) e).getStack(i);
				if (existingStack.isEmpty()) {
					int moveCount = Math.min(stack.getCount(), maxInvPerSlot);
					((Inventory) e).setStack(i, stack.split(moveCount));
					e.markDirty();
					if (moveCount >= stack.getCount()) return true;
					continue;
				}
				if (!canMergeItems(existingStack, stack, maxInvPerSlot)) continue;
				int moveCount = Math.min(stack.getCount(), existingStack.getMaxCount() - existingStack.getCount());
				if (moveCount < 0) continue;
				stack.decrement(moveCount);
				existingStack.increment(moveCount);
				e.markDirty();
				if (stack.isEmpty()) return true;
			}
		}
		return false;
	}

	public static boolean canMergeItems(ItemStack stak1, ItemStack stak2, int slotLimit) {
		if (!stak1.isOf(stak2.getItem()))
			return false;
		if (stak1.getDamage() != stak2.getDamage())
			return false;
		if (stak1.getCount() >= Math.min(stak1.getMaxCount(), slotLimit))
			return false;
		return ItemStack.areNbtEqual(stak1, stak2);
	}

	public void dropInv() {
		if (world == null) return;
		if (itemQueue.isEmpty()) return;
		TransportedStack entry = itemQueue.poll();
		while (entry != null) {
			world.spawnEntity(new ItemEntity(world, pos.getX()+.5, pos.getY()+1, pos.getZ()+.5, entry.stack));
			entry = itemQueue.poll();
		}
	}

	@Override
	public boolean acceptItemFrom(TransportedStack stack, Direction dir) {
		if (world == null) return false;
		stack.travelTime = world.getLevelProperties().getTime() + getPipeTransferTime();
		stack.origin = dir;
		itemQueue.offer(stack);
		world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, stack.travelTime + 1, world.getTickOrder()));
		markDirty();
		return true;
	}

	@Environment(EnvType.CLIENT)
	public AbstractPipeRenderer.DisconnectedSideLinesRenderer getDisconnectedSideLinesRenderer(Direction dir) {
		return (connectedSides & (1 << dir.ordinal())) == 0 ? AbstractPipeRenderer::drawDisconnectedSideLines : null;
	}
	public int getPipeTransferTime() {
		return 10;
	}
	abstract public Block asBlock();

}
