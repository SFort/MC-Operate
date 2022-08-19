package tf.ssf.sfort.operate.pipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;

public class PriorityPipe extends AbstractPipe{
	public static Block BLOCK;

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PriorityPipeEntity(pos, state);
	}

	public static void register() {
		if (false) return;
		BLOCK = Registry.register(Registry.BLOCK, Main.id("priority_pipe"), new PriorityPipe());
		PriorityPipeEntity.register();
		if (true) {
			Spoon.CRAFT.put(new Pair<>(Blocks.COBWEB, Blocks.STONE), (world, pos, cpos, state, cstate) -> {
				world.removeBlock(pos, false);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, Sounds.SPOON_BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(cpos, BLOCK.getDefaultState());
				return true;
			});
		}
	}
	@Override public Item asItem(){return Items.COBWEB;}

}
