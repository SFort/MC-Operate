package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;
import tf.ssf.sfort.operate.util.OperateUtil;

import java.util.LinkedHashMap;


public class RequestPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<RequestPipeEntity> ENTITY_TYPE;

	public LinkedHashMap<BlockPos, Inventory> discoveredInventories = new LinkedHashMap<>();

	public RequestPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("request_pipe"), FabricBlockEntityTypeBuilder.create(RequestPipeEntity::new, RequestPipe.BLOCK).build(null));
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		{
			NbtCompound disc = new NbtCompound();
			discoveredInventories.forEach(
					(pos, inv) -> disc.put(String.valueOf(disc.getSize()), NbtHelper.fromBlockPos(pos))
			);
			tag.put("request$disc", disc);
		}
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		{
			NbtCompound disc = new NbtCompound();
			discoveredInventories.forEach(
					(pos, inv) -> disc.put(String.valueOf(disc.getSize()), NbtHelper.fromBlockPos(pos))
			);
			tag.put("request$disc", disc);
		}
	}

	@Override
	public void readNbtCommon(NbtCompound tag) {
		super.readNbtCommon(tag);
		if (world != null) {
			discoveredInventories.clear();
			NbtCompound disc = tag.getCompound("request$disc");
			int i=0;
			while (true) {
				NbtCompound posTag = disc.getCompound(Integer.toString(i++));
				if (posTag.isEmpty()) break;
				BlockPos pos = NbtHelper.toBlockPos(posTag);
				BlockEntity entity = world.getBlockEntity(pos);
				if (!(entity instanceof Inventory)) break;
				discoveredInventories.put(pos, (Inventory) entity);
			}
		}
	}

	public void reloadDiscoveredInv() {
		markDirty();
		discoveredInventories.clear();
		BlockPos.Mutable ePos = new BlockPos.Mutable();
		for (AbstractPipeEntity entity : OperateUtil.getConnecting(world, pos, AbstractPipeEntity.class, AbstractPipeEntity::isConnected)) {
			if (entity instanceof OverseerPipeEntity) {
				BlockState state = world.getBlockState(ePos.set(entity.getPos()));
				if (state.isOf(OverseerPipe.BLOCK)) {
					BlockEntity be = world.getBlockEntity(ePos.offset(state.get(OverseerPipe.FACING)));
					if (be instanceof Inventory) {
						discoveredInventories.put(be.getPos(), (Inventory)be);
					}
				}
			}
		}
	}


	@Override
	public Block asBlock() {
		return RequestPipe.BLOCK;
	}

}
