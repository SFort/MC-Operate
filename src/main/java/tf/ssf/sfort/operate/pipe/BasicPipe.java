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
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;

public class BasicPipe extends AbstractPipe{
	public static Block BLOCK;

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new BasicPipeEntity(pos, state);
	}

	public static void register() {
		if (Config.basicPipe == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BLOCK = Registry.register(Registries.BLOCK, Main.id("basic_pipe"), new BasicPipe());
		BasicPipeEntity.register();
		if (Config.basicPipe == Config.EnumOnOffUnregistered.ON) {
			Spoon.INFUSE.put(new Pair<>(Items.IRON_INGOT, Blocks.BLACKSTONE), (world, pos, state, offhand, context) -> {
				offhand.decrement(1);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
					world.playSound(null, pos, Sounds.SPOON_HIT, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(pos, BasicPipe.BLOCK.getDefaultState());
				return ActionResult.SUCCESS;
			});
		}

	}
	@Override public Item asItem(){return Items.IRON_INGOT;}

}
