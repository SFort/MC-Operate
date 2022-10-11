package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.tick.OrderedTick;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;
import tf.ssf.sfort.operate.pipe.DroppingItemPipeAcceptor;
import tf.ssf.sfort.operate.pipe.FilterPipeEntity;
import tf.ssf.sfort.operate.pipe.GuidedTransportedStack;
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
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("request_pipe"), FabricBlockEntityTypeBuilder.create(RequestPipeEntity::new, RequestPipe.BLOCK).build(null));
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
		if (world == null) return;
		progressRequestQue();
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

	public void playerInteraction(double x, double y, boolean correctSide) {
		if (rpui == null) {
			reloadCache();
			rpui = new RequestPipeUi(world, cache);
			markDirtyClient();
			return;
		}
		if (correctSide) {
			markDirtyClient();
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
		markDirtyClient();
	}

	public boolean isBusy() {
		return requestQue != null;
	}


	@Override
	public Block asBlock() {
		return RequestPipe.BLOCK;
	}

}
