package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;

public class OverseerPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<OverseerPipeEntity> ENTITY_TYPE;

	public OverseerPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("overseer_pipe"), FabricBlockEntityTypeBuilder.create(OverseerPipeEntity::new, OverseerPipe.BLOCK).build(null));
	}

	@Override
	public Block asBlock() {
		return OverseerPipe.BLOCK;
	}
}
