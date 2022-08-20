package tf.ssf.sfort.operate.pipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.tube.ColorTubeEntity;
import tf.ssf.sfort.operate.tube.TubeConnectTypes;

public class FilterPipe extends AbstractPipe{
	public static Block BLOCK;

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FilterPipeEntity(pos, state);
	}

	public static void register() {
		if (false) return;
		BLOCK = Registry.register(Registry.BLOCK, Main.id("filter_pipe"), new FilterPipe());
		FilterPipeEntity.register();
		if (true) {
			Spoon.INFUSE.put(new Pair<>(Items.GOLD_INGOT, BasicPipe.BLOCK), (world, pos, state, offhand) -> {
				offhand.decrement(1);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, Sounds.SPOON_BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
					world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(pos, BLOCK.getDefaultState());
				return ActionResult.SUCCESS;
			});
		}
	}

	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (world.isClient) return ActionResult.CONSUME;
		if (blockState.isOf(BLOCK) && hand == Hand.MAIN_HAND) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e instanceof FilterPipeEntity) {
				ItemStack stack = player.getMainHandStack();
				if (stack != null  && !stack.isOf(Spoon.ITEM)) {
					if (player.getOffHandStack().isOf(Spoon.ITEM)) ((FilterPipeEntity) e).filterInSides[blockHitResult.getSide().ordinal()] = stack.getItem();
					else ((FilterPipeEntity) e).filterOutSides[blockHitResult.getSide().ordinal()] = stack.getItem();
					e.markDirty();
					return ActionResult.SUCCESS;
				}
			}
		}
		return ActionResult.PASS;
	}

	@Override public Item asItem(){return Items.GOLD_INGOT;}

}
