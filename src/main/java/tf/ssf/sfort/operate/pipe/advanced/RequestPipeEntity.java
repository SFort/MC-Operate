package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.tick.OrderedTick;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;
import tf.ssf.sfort.operate.pipe.DroppingItemPipeAcceptor;
import tf.ssf.sfort.operate.pipe.FilterPipeEntity;
import tf.ssf.sfort.operate.pipe.GuidedTransportedStack;
import tf.ssf.sfort.operate.pipe.advanced.util.PipePathing;
import tf.ssf.sfort.operate.util.OperateUtil;
import tf.ssf.sfort.operate.pipe.advanced.util.RequestPipeRequest;
import tf.ssf.sfort.operate.pipe.advanced.util.RequestPipeCache;


public class RequestPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<RequestPipeEntity> ENTITY_TYPE;

	public RequestPipeCache cache = new RequestPipeCache();
	public RequestPipeRequest requestQue = null;

	public RequestPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("request_pipe"), FabricBlockEntityTypeBuilder.create(RequestPipeEntity::new, RequestPipe.BLOCK).build(null));
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
	}

	@Override
	public void readNbtCommon(NbtCompound tag) {
		super.readNbtCommon(tag);
	}

	public void reloadCache() {
		cache = new RequestPipeCache();
		BlockPos.Mutable ePos = new BlockPos.Mutable();
		PipePathing.Builder pathing = new PipePathing.Builder();
		for (AbstractPipeEntity entity : OperateUtil.getConnectingPath(world, pos, AbstractPipeEntity.class, (e, dir) -> !(e instanceof FilterPipeEntity) && e.isConnected(dir), this, pathing)) {
			if (entity instanceof OverseerPipeEntity) {
				BlockState state = world.getBlockState(ePos.set(entity.getPos()));
				if (state.isOf(OverseerPipe.BLOCK)) {
					Direction dir = state.get(OverseerPipe.FACING);
					cache.push(entity.getPos(), dir);
				}
			}
		}
		cache.pathing = pathing.build();
		markDirty();
	}
	@Override
	public void pipeTick() {
		super.pipeTick();
		if (world == null) return;
		progressRequestQue();
	}
	public void progressRequestQue(){
		if (requestQue == null) return;
		int ret = cache.firstEntry(world, this::tryInvExtract);
		if ((ret & 1) != 0) markDirty();
		if ((ret & 2) != 0) requestQue = null;
		if (requestQue != null) {
			world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, world.getLevelProperties().getTime() + 1, world.getTickOrder()));
		}
	}
	public boolean tryInvExtract(RequestPipeCache.Entry entry) {
		if (entry.dir == null) return false;
		Inventory inv = entry.getInv();
		if (inv == null) return false;
		BlockEntity pipe = world.getBlockEntity(entry.pos);
		if (pipe instanceof DroppingItemPipeAcceptor) {
			for (int i = 0, size = inv.size(); i < size; i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty()) continue;
				if (requestQue.test(stack)){
					stack = inv.removeStack(i);
					if (requestQue != RequestPipeRequest.ALL) {
						requestQue.count -= stack.getCount();
						if (requestQue.count<=0) {
							requestQue = requestQue.next;
						}
					}
					((DroppingItemPipeAcceptor) pipe).alwaysAcceptItemFrom(new GuidedTransportedStack(stack, entry.dir, 0, new PipePathing(cache.pathing.get(entry.pos))), entry.dir);
					return true;
				}
			}
		}
		return false;
	}
	public void requestAll(){
		requestQue = RequestPipeRequest.ALL;
		world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, world.getLevelProperties().getTime() + 1, world.getTickOrder()));
	}

	public boolean isBusy() {
		return requestQue != null;
	}


	@Override
	public Block asBlock() {
		return RequestPipe.BLOCK;
	}

}
