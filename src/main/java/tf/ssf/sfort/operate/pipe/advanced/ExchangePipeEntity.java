package tf.ssf.sfort.operate.pipe.advanced;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;

import java.util.HashMap;
import java.util.Map;

public class ExchangePipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<ExchangePipeEntity> ENTITY_TYPE;
	public Map<Item, Integer> provides = new HashMap<>();
	public Map<Item, Integer> requires = new HashMap<>();
	public ExchangePipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("exchange_pipe"), FabricBlockEntityTypeBuilder.create(ExchangePipeEntity::new, ExchangePipe.BLOCK).build(null));
	}

	@Override
	public Block asBlock() {
		return ExchangePipe.BLOCK;
	}
}
