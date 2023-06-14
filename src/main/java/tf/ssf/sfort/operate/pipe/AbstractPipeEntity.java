package tf.ssf.sfort.operate.pipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
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
import tf.ssf.sfort.operate.pipe.util.GuidedTransportedStack;
import tf.ssf.sfort.operate.pipe.util.ItemPipeMarkNonBorderSync;
import tf.ssf.sfort.operate.pipe.util.OperatePipeUpdateS2CPacket;
import tf.ssf.sfort.operate.pipe.util.DroppingItemPipeAcceptor;
import tf.ssf.sfort.operate.pipe.util.ItemPipeAcceptor;
import tf.ssf.sfort.operate.pipe.util.TransportedStackList;
import tf.ssf.sfort.operate.pipe.util.TransportedStack;
import tf.ssf.sfort.operate.util.SyncableLinkedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public abstract class AbstractPipeEntity extends BlockEntity implements ItemPipeAcceptor, ItemPipeMarkNonBorderSync, DroppingItemPipeAcceptor {
	public enum AccFlags{
		HAS_DIRTY_CLIENT, HAS_DIRTY_SERVER, HAS_SCHEDULED_TICK, SHOULD_NON_BORDER_SYNC;
		public static final int DIRTY_SERVER_CLIENT_FLAG = HAS_DIRTY_CLIENT.flag | HAS_DIRTY_SERVER.flag;

		public final int flag;
		public final int mask;
		AccFlags() {
			flag = 1 << this.ordinal();
			mask = ~flag;
		}
		public int clearFlag(int flags) {
			return flags & mask;
		}
	}
	public byte connectedSidesByte = 0;
	public int accFlags = 0;
	public Direction[] connectedSides = new Direction[0];

	public final TransportedStackList itemQueue = new TransportedStackList();

	public AbstractPipeEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		accFlags = AccFlags.HAS_DIRTY_CLIENT.clearFlag(accFlags);
		if (itemQueue.isSyncable()) {
			if ((accFlags & AccFlags.SHOULD_NON_BORDER_SYNC.flag) != 0) {
				accFlags = AccFlags.SHOULD_NON_BORDER_SYNC.clearFlag(accFlags);
				return BlockEntityUpdateS2CPacket.create(
						this,
						b -> this.clientItemUpdateTag()
				);
			}
			return new OperatePipeUpdateS2CPacket(BlockEntityUpdateS2CPacket.create(
					this,
					b -> this.clientItemUpdateTag()
			));
		}
		return BlockEntityUpdateS2CPacket.create(this);
	}
	public NbtCompound clientItemUpdateTag() {
		NbtCompound tag = new NbtCompound();
		NbtCompound items = new NbtCompound();
		int i=0;
		Supplier<NbtCompound> stack = itemQueue.popSync();
		while (stack != null) {
			items.put(Integer.toString(i++), stack.get());
			stack = itemQueue.popSync();
		}
		tag.put("addQ", items);
		return tag;
	}

	@Override
	public void markPipeNonBorderSync() {
		accFlags |= AccFlags.SHOULD_NON_BORDER_SYNC.flag;
	}
	@Override
	public void markDirty() {
		itemQueue.lockSync();
		partialMarkDirty();
	}
	public void markDirtyClient() {
		itemQueue.lockSync();
		if (world != null && !world.isClient()) {
			((ServerWorld) world).getChunkManager().markForUpdate(getPos());
		}
	}
	public void markDirtyServer() {
		if (world != null) {
			world.markDirty(pos);
		}
	}
	public void partialMarkDirty() {
		if (world != null) {
			world.markDirty(pos);
			if (!world.isClient()) {
				((ServerWorld) world).getChunkManager().markForUpdate(getPos());
			}
		}
	}
	public void partialMarkDirtyClient() {
		if (world != null && !world.isClient()) {
			((ServerWorld) world).getChunkManager().markForUpdate(getPos());
		}
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = new NbtCompound();
		tag.putByte("sides", connectedSidesByte);
		NbtCompound items = new NbtCompound();
		int i=0;
		{
			itemQueue.oldestRequiredSync = itemQueue.first;
			for (Supplier<NbtCompound> stack = itemQueue.popSync(); stack != null; stack = itemQueue.popSync()) {
				items.put(Integer.toString(i++), stack.get());
			}
		}
		tag.put("items", items);
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		accFlags = AccFlags.HAS_DIRTY_SERVER.clearFlag(accFlags);
		super.writeNbt(tag);
		tag.putByte("sides", connectedSidesByte);
		NbtCompound items = new NbtCompound();
		int i=0;
		for (SyncableLinkedList.Node<TransportedStack> stack = itemQueue.first; stack != null; stack=stack.next) {
			items.put(Integer.toString(i++), stack.item.toTag(new NbtCompound()));
		}
		tag.put("items", items);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		if (world != null && world.isClient() && readNbtClient(tag)) {
			return;
		}
		readNbtCommon(tag);
		markDirty();
	}
	public boolean readNbtClient(NbtCompound tag) {
		if (tag.contains("addQ", NbtElement.COMPOUND_TYPE)) {
			NbtCompound items = tag.getCompound("addQ");
			int i=0;
			while (true) {
				NbtCompound item = items.getCompound(Integer.toString(i++));
				if (item.isEmpty()) break;
				itemQueue.push(TransportedStack.fromNbt(item));
			}
			itemQueue.oldestRequiredSync = null;
			return true;
		}
		itemQueue.oldestRequiredSync = null;
		return false;
	}
	public void readNbtCommon(NbtCompound tag) {
		{
			connectedSidesByte = tag.getByte("sides");
			Direction[] readingSides = new Direction[Integer.bitCount(connectedSidesByte & 0b111111)];
			int i = 0;
			for (Direction d : Direction.values()) {
				if ((connectedSidesByte & (1 << d.ordinal())) != 0) readingSides[i++] = d;
			}
			connectedSides = readingSides;
		}
		itemQueue.clear();
		NbtCompound items = tag.getCompound("items");
		int i=0;
		while (true) {
			NbtCompound item = items.getCompound(Integer.toString(i++));
			if (item.isEmpty()) break;
			itemQueue.push(TransportedStack.fromNbt(item));
		}
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
		markDirty();
		toggleConnection(side);
	}
	public void toggleConnection(Direction side) {
		connectedSidesByte ^= 1 << side.ordinal();
		for (int i=0;i<connectedSides.length;i++) {
			if (connectedSides[i] != side) continue;
			Direction[] n = new Direction[connectedSides.length-1];
			System.arraycopy(connectedSides, 0, n, 0, i);
			if (i < n.length) System.arraycopy(connectedSides, i+1, n, i, connectedSides.length-i-1);
			connectedSides = n;
			return;
		}
		Direction[] n = new Direction[connectedSides.length+1];
		System.arraycopy(connectedSides, 0, n, 0, connectedSides.length);
		n[connectedSides.length] = side;
		connectedSides = n;
	}
	public Function<TransportedStack, List<Direction>> getOutputs(){
		AtomicReference<Direction> lastDir = new AtomicReference<>();
		List<Direction> ret = new ArrayList<>();
		return stack -> {
			if (lastDir.get() != stack.origin) {
				lastDir.set(stack.origin);
				ret.clear();
				for (Direction d : connectedSides) {
					if (stack.origin != d) ret.add(d);
				}
			}
			Collections.shuffle(ret);
			return ret;
		};
	}

	public void pipeTick() {
		if (world == null) return;
		if (world instanceof ServerWorld && Config.chunkLoadPipes) {
			((ServerWorld)world).getChunkManager().addTicket(Main.PIPE_TICKET_TYPE, new ChunkPos(pos), 3, pos);
		}
		long nextTransfer = world instanceof ServerWorld ? progressQueue() : progressQueueClient();
		if (nextTransfer >= 0) {
			world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, nextTransfer + 1, world.getTickOrder()));
		} else {
			accFlags = AccFlags.HAS_SCHEDULED_TICK.clearFlag(accFlags);
		}
	}
	public long progressQueueClient() {
		if (world == null) return -1;
		if (itemQueue.isEmpty()) return -1;
		TransportedStack entry = itemQueue.first.item;
		long worldTime = world.getLevelProperties().getTime();
		if (entry.travelTime <= worldTime) {
			Function<TransportedStack, List<Direction>> outputsFunc = getOutputs();
			do {
				List<Direction> outputs = outputsFunc.apply(entry);
				if (!(entry instanceof GuidedTransportedStack) && outputs.size() == 1){
					transportStack(entry, outputs.get(0), false);
				}
				itemQueue.progress();
				if (itemQueue.isEmpty()) {
					return -1;
				} else {
					entry = itemQueue.first.item;
				}
			} while (entry.travelTime <= worldTime);
		}
		return entry.travelTime;
	}
	public long progressQueue() {
		if (world == null) return -1;
		if (itemQueue.isEmpty()) return -1;

		TransportedStack entry = itemQueue.first.item;
		long worldTime = world.getLevelProperties().getTime();
		if (entry.travelTime <= worldTime) {
			Function<TransportedStack, List<Direction>> outputsFunc = getOutputs();
			do {
				transport:
				{
					List<Direction> outputs = outputsFunc.apply(entry);
					Direction preferredPath = entry.getPreferredPath(outputs, pos);
					if (preferredPath != null && outputs.contains(preferredPath) && transportStack(entry, preferredPath, true))
						break transport;
					if (transportStack(entry, outputs, outputs.size() != 1)) {
						break transport;
					}
					dropTransportedStack(entry);


				}
				itemQueue.progress();
				markDirtyServer();
				if (itemQueue.isEmpty()) {
					return -1;
				} else {
					entry = itemQueue.first.item;
				}
			} while (entry.travelTime <= worldTime);
		}
		return entry.travelTime;
	}
	public void dropTransportedStack(TransportedStack stack) {
		dropTransportedStack(stack, stack.origin.getOpposite().getVector());
	}
	public void dropTransportedStack(TransportedStack stack, Vec3i dropDir) {
		ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5 + (dropDir.getX() >> 1), pos.getY() + .5 + (dropDir.getY() >> 1), pos.getZ() + .5 + (dropDir.getZ() >> 1), stack.stack);
		itemEntity.addVelocity(dropDir.getX(), dropDir.getY(), dropDir.getZ());
		world.spawnEntity(itemEntity);
	}
	public boolean transportStack(TransportedStack entry, List<Direction> sides, boolean markTargetPipe) {
		for (Direction dir : sides) {
			if (transportStack(entry, dir, markTargetPipe)) return true;
		}
		return false;
	}
	public boolean transportStack(TransportedStack entry, Direction dir, boolean markTargetPipe) {
		BlockPos offset = pos.offset(dir);
		BlockEntity e = world.getBlockEntity(offset);
		if (e == null) {
			if (world.getBlockState(offset).getCollisionShape(world, offset).isEmpty()) {
				dropTransportedStack(entry, dir.getVector());
				return true;
			}
			return false;
		}
		if (e instanceof ItemPipeAcceptor && ((ItemPipeAcceptor) e).acceptItemFrom(entry, dir.getOpposite())) {
			if (markTargetPipe && e instanceof ItemPipeMarkNonBorderSync) ((ItemPipeMarkNonBorderSync) e).markPipeNonBorderSync();
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
		return Objects.equals(stak1, stak2);
	}

	public void dropInv() {
		if (world == null) return;
		if (itemQueue.isEmpty()) return;
		TransportedStack entry = itemQueue.pop();
		while (entry != null) {
			world.spawnEntity(new ItemEntity(world, pos.getX()+.5, pos.getY()+1, pos.getZ()+.5, entry.stack));
			entry = itemQueue.pop();
		}
	}

	@Override
	public boolean acceptItemFrom(TransportedStack stack, Direction dir) {
		if (world == null) return false;
		stack.travelTime = world.getLevelProperties().getTime() + getPipeTransferTime();
		stack.origin = dir;
		itemQueue.push(stack);
		if ((accFlags & AccFlags.HAS_SCHEDULED_TICK.flag) == 0) {
			world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, stack.travelTime + 1, world.getTickOrder()));
		}
		switch (accFlags & AccFlags.DIRTY_SERVER_CLIENT_FLAG) {
			case 0 -> partialMarkDirty();
			case 0b1 -> markDirtyServer();
			case 0b10 -> partialMarkDirtyClient();
		}
		accFlags |= 0b111;
		return true;
	}
	@Override
	public void alwaysAcceptItemFrom(TransportedStack stack, Direction dir){
		if (!acceptItemFrom(stack, dir)) {
			dropTransportedStack(stack);
		}
	}

	public boolean isConnected(Direction dir) {
		return (connectedSidesByte & (1 << dir.ordinal())) != 0;
	}
	public int getPipeTransferTime() {
		return 10;
	}
	abstract public Block asBlock();

}
