package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;


public class UnloadPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<UnloadPipeEntity> ENTITY_TYPE;

	public UnloadPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("unloading_pipe"), FabricBlockEntityTypeBuilder.create(UnloadPipeEntity::new, UnloadPipe.BLOCK).build(null));
	}

	@Override
	public int getPipeTransferTime() {
		return 10;
	}

	@Override
	public Block asBlock() {
		return UnloadPipe.BLOCK;
	}
}
