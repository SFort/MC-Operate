package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;

public class OverseerPipeEntity extends AbstractPipeEntity{
	public static BlockEntityType<OverseerPipeEntity> ENTITY_TYPE;

	public OverseerPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("overseer_pipe"), FabricBlockEntityTypeBuilder.create(OverseerPipeEntity::new, OverseerPipe.BLOCK).build(null));
	}

	@Override
	public Block asBlock() {
		return OverseerPipe.BLOCK;
	}
}
