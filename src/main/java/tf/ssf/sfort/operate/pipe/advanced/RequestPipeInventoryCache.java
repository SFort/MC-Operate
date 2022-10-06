package tf.ssf.sfort.operate.pipe.advanced;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

public class RequestPipeInventoryCache {
	public static RequestPipeInventoryCache UNLOADED = new RequestPipeInventoryCache(null);
	public static Inventory getInv(RequestPipeEntity entity, BlockPos pos) {
		World world = entity.getWorld();
		if (world == null) return null;
		RequestPipeInventoryCache cache = entity.discoveredOverseers.get(pos);
		if (cache == null) {
			entity.discoveredOverseers.remove(pos);
			entity.markDirty();
			return null;
		}
		if (cache == UNLOADED) {
			entity.markDirty();
			BlockState state = world.getBlockState(pos);
			if (state.isOf(OverseerPipe.BLOCK)) {
				cache = new RequestPipeInventoryCache(state.get(OverseerPipe.FACING));
				entity.discoveredOverseers.put(pos, cache);
			} else {
				entity.discoveredOverseers.remove(pos);
				return null;
			}
		}
		if (cache.inv != null) {
			Inventory inv = cache.inv.get();
			if (inv != null) {
				return inv;
			}
		}
		BlockEntity be = world.getBlockEntity(pos.offset(cache.dir));
		if (be instanceof Inventory) {
			cache.inv = new WeakReference<>((Inventory)be);
			return (Inventory)be;
		}
		return null;
	}
	public final Direction dir;
	public WeakReference<Inventory> inv;
	public RequestPipeInventoryCache(Direction dir) {
		this.dir = dir;
	}
}
