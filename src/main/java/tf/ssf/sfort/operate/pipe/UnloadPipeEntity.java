package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import tf.ssf.sfort.operate.Main;


public class UnloadPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<UnloadPipeEntity> ENTITY_TYPE;

	public UnloadPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("unloading_pipe"), FabricBlockEntityTypeBuilder.create(UnloadPipeEntity::new, UnloadPipe.BLOCK).build(null));
	}

	@Override
	public Block asBlock() {
		return UnloadPipe.BLOCK;
	}
}
