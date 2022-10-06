package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;
import tf.ssf.sfort.operate.util.OperateUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RequestPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<RequestPipeEntity> ENTITY_TYPE;

	public LinkedHashMap<BlockPos, RequestPipeInventoryCache> discoveredOverseers = new LinkedHashMap<>();

	public RequestPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("request_pipe"), FabricBlockEntityTypeBuilder.create(RequestPipeEntity::new, RequestPipe.BLOCK).build(null));
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		{
			NbtCompound disc = new NbtCompound();
			discoveredOverseers.forEach(
					(pos, inv) -> disc.put(String.valueOf(disc.getSize()), NbtHelper.fromBlockPos(pos))
			);
			tag.put("request$disc", disc);
		}
	}

	@Override
	public void readNbtCommon(NbtCompound tag) {
		super.readNbtCommon(tag);
		discoveredOverseers.clear();
		NbtCompound disc = tag.getCompound("request$disc");
		int i = 0;
		while (true) {
			NbtCompound posTag = disc.getCompound(Integer.toString(i++));
			if (posTag.isEmpty()) break;
			BlockPos pos = NbtHelper.toBlockPos(posTag);
			discoveredOverseers.put(pos, RequestPipeInventoryCache.UNLOADED);
		}
	}

	public void reloadDiscoveredInv() {
		discoveredOverseers.clear();
		BlockPos.Mutable ePos = new BlockPos.Mutable();
		for (AbstractPipeEntity entity : OperateUtil.getConnecting(world, pos, AbstractPipeEntity.class, AbstractPipeEntity::isConnected, this)) {
			if (entity instanceof OverseerPipeEntity) {
				BlockState state = world.getBlockState(ePos.set(entity.getPos()));
				if (state.isOf(OverseerPipe.BLOCK)) {
					Direction dir = state.get(OverseerPipe.FACING);
					discoveredOverseers.put(entity.getPos(), new RequestPipeInventoryCache(dir));
				}
			}
		}
		markDirty();
	}


	@Override
	public Block asBlock() {
		return RequestPipe.BLOCK;
	}

}
