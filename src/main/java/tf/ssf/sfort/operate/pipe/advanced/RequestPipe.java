package tf.ssf.sfort.operate.pipe.advanced;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.pipe.AbstractPipe;
import tf.ssf.sfort.operate.pipe.BasicPipe;

public class RequestPipe extends AbstractPipe {
	public static Block BLOCK;

	public RequestPipe() {
		super(Settings.of(Material.PISTON).strength(1.5F));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new RequestPipeEntity(pos, state);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof RequestPipeEntity) {
			if (((RequestPipeEntity) be).isBusy()) return ActionResult.CONSUME;
			((RequestPipeEntity) be).reloadCache();
			if (player.getStackInHand(hand).isOf(Items.LAVA_BUCKET)) {
				((RequestPipeEntity) be).requestAll();
			}
		}
		return ActionResult.CONSUME;
	}

	public static void register() {
		if (Config.advancedPipe == null) return;
		BLOCK = Registry.register(Registry.BLOCK, Main.id("request_pipe"), new RequestPipe());
		RequestPipeEntity.register();
		if (Config.advancedPipe) {
			Spoon.INFUSE.put(new Pair<>(Items.RECOVERY_COMPASS, BasicPipe.BLOCK), (world, pos, state, offhand, context) -> {
				offhand.decrement(1);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
					world.playSound(null, pos, Sounds.SPOON_HIT, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(pos, RequestPipe.BLOCK.getDefaultState());
				return ActionResult.SUCCESS;
			});
		}

	}
	@Override public Item asItem(){return Items.IRON_INGOT;}

}
