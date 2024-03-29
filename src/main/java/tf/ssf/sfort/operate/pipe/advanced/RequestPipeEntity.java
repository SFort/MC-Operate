package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.tick.OrderedTick;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;
import tf.ssf.sfort.operate.pipe.util.DroppingItemPipeAcceptor;
import tf.ssf.sfort.operate.pipe.FilterPipeEntity;
import tf.ssf.sfort.operate.pipe.util.GuidedTransportedStack;
import tf.ssf.sfort.operate.pipe.advanced.util.PipePathing;
import tf.ssf.sfort.operate.pipe.advanced.util.RequestPipeCache;
import tf.ssf.sfort.operate.pipe.advanced.util.RequestPipeRequest;
import tf.ssf.sfort.operate.pipe.advanced.util.RequestPipeUi;
import tf.ssf.sfort.operate.util.OperateUtil;


public class RequestPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<RequestPipeEntity> ENTITY_TYPE;

	public RequestPipeCache cache = null;
	public RequestPipeUi rpui = null;
	public RequestPipeRequest requestQue = null;

	public RequestPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("request_pipe"), FabricBlockEntityTypeBuilder.create(RequestPipeEntity::new, RequestPipe.BLOCK).build(null));
	}
	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		tag.put("RequestPipeUserInterface", rpui == null ? new NbtCompound() : rpui.toNbt(new NbtCompound()));
		return tag;
	}
	@Override
	public boolean readNbtClient(NbtCompound tag) {
		boolean ret = super.readNbtClient(tag);
		if (!ret) rpui = RequestPipeUi.fromNbt(tag.getCompound("RequestPipeUserInterface"));
		return ret;
	}
	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		if (requestQue != null) tag.put("requestQue", requestQue.toNbt(new NbtCompound()));
	}

	@Override
	public void readNbtCommon(NbtCompound tag) {
		super.readNbtCommon(tag);
		requestQue = RequestPipeRequest.fromNbt(tag.getCompound("requestQue"));

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
	}
	@Override
	public void pipeTick() {
		super.pipeTick();
		if (world instanceof ServerWorld) {
			progressRequestQue();
		}
	}
	public void progressRequestQue(){
		if (requestQue == null) return;
		if (cache == null) reloadCache();
		if (cache.firstEntry(world, this::tryInvExtract)){
			requestQue = null;
			markDirtyServer();
		}
		if (requestQue == null) {
			cache = null;
		} else {
			world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, world.getLevelProperties().getTime() + 1, world.getTickOrder()));
		}
	}
	public boolean tryInvExtract(RequestPipeCache.InvNode invNode) {
		if (invNode.dir == null) return false;
		Inventory inv = invNode.getInv();
		if (inv == null) return false;
		BlockEntity pipe = world.getBlockEntity(invNode.pos);
		if (pipe instanceof DroppingItemPipeAcceptor) {
			for (int i = 0, size = inv.size(); i < size; i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty()) continue;
				if (requestQue.test(stack)){
					stack = inv.removeStack(i);
					if (requestQue.subtract(stack.getCount())) {
						if (requestQue.count < 0) {
							ItemStack leave = stack.split(-requestQue.count);
							inv.setStack(i, leave);
							if (inv.getStack(i).isEmpty()) stack.setCount(stack.getCount()+-requestQue.count);
						}
						requestQue = requestQue.next;
						markDirtyServer();
					}
					((DroppingItemPipeAcceptor) pipe).alwaysAcceptItemFrom(new GuidedTransportedStack(stack, invNode.dir, 0, new PipePathing(cache.pathing.get(invNode.pos))), invNode.dir);
					return true;
				}
			}
		}
		return false;
	}
	public void requestAll(){
		requestQue = RequestPipeRequest.ALL;
		if (rpui != null) markDirtyClient();
		rpui = null;
		world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, world.getLevelProperties().getTime() + 1, world.getTickOrder()));
		markDirtyServer();
	}

	public void playerInteraction(double x, double y, Direction direction) {
		markDirtyClient();
		if (rpui == null) {
			reloadCache();
			rpui = new RequestPipeUi(world, direction, cache);
			return;
		}
		if (rpui.direction == direction) {
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.PLAYERS, .5f, .6f+world.random.nextFloat()*.2f);
			if (rpui.playerClickedSlot(((int)Math.floor(x*5)) + ((int)Math.floor(y*5))*5)) return;
			RequestPipeRequest req = rpui.generateRequests();
			if (req != null) {
				requestQue = req;
				world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, world.getLevelProperties().getTime() + 1, world.getTickOrder()));
				markDirtyServer();
			}
		}
		rpui = null;
		cache = null;
	}

	public boolean isBusy() {
		return requestQue != null;
	}


	@Override
	public Block asBlock() {
		return RequestPipe.BLOCK;
	}

	public void playerClearFilter() {
		if (rpui != null){
			rpui.clearFilter();
			markDirtyClient();
		}
	}

	public void playerType(char c) {
		if (rpui != null){
			rpui.addFilter(c);
			markDirtyClient();
		}
	}
}
