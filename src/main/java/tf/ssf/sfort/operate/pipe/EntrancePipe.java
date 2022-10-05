package tf.ssf.sfort.operate.pipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.util.OperateUtil;

public class EntrancePipe extends AbstractPipe{
	public static Block BLOCK;

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EntrancePipeEntity(pos, state);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity instanceof ItemEntity && entity.isAlive()) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof EntrancePipeEntity) {
				ItemStack stack = ((ItemEntity) entity).getStack();
				Vec3d epos = entity.getPos();
				Direction dir = OperateUtil.dirFromVec(epos.x - pos.getX(), epos.y - pos.getY(), epos.z - pos.getZ());
				if (((EntrancePipeEntity) be).acceptItemFrom(stack, dir)) entity.kill();
				else entity.addVelocity(world.random.nextDouble() - .5, dir == Direction.UP ? .5 : 0, world.random.nextDouble() - .5);
			}
		}
	}

	public static void register() {
		if (Config.basicPipe == null) return;
		BLOCK = Registry.register(Registry.BLOCK, Main.id("entrance_pipe"), new EntrancePipe());
		EntrancePipeEntity.register();
		if (Config.basicPipe) {
			Spoon.CRAFT.put(new Pair<>(Blocks.IRON_BLOCK, Blocks.BLACKSTONE), (world, pos, cpos, state, cstate) -> {
				world.removeBlock(pos, false);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, Sounds.SPOON_BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(cpos, EntrancePipe.BLOCK.getDefaultState());
				return true;
			});
		}

	}
	@Override public Item asItem(){return Items.IRON_INGOT;}

}
