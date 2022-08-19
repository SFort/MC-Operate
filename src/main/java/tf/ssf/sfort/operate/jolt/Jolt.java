package tf.ssf.sfort.operate.jolt;


import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;

public class Jolt extends Block implements BlockEntityProvider {
	public static Block BLOCK;
	public Jolt() {
		super(Settings.of(Material.STONE, MapColor.BLACK).requiresTool().strength(50.0F, Blocks.OBSIDIAN.getBlastResistance()));
	}
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof JoltEntity) {
			BlockPos s = fromPos.subtract(pos);
			((JoltEntity) e).update_dir(Direction.fromVector(s.getX(),s.getY(),s.getZ()));
		}
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof JoltEntity)
				((JoltEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient && (stack.isEmpty() || stack.getItem() instanceof BlockItem)) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e!=null) {
				player.setStackInHand(hand, ItemStack.EMPTY);
				((JoltEntity)e).replaceStack(stack);
				e.markDirty();
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.CONSUME;
	}
	public Jolt(Settings settings) { super(settings); }
	public static void register() {
		if (Config.jolt != null) {
			BLOCK = Registry.register(Registry.BLOCK, Main.id("jolt"), new Jolt());
			JoltEntity.register();
			if (Config.jolt)
				Spoon.CRAFT.put(new Pair<>(Blocks.SOUL_SAND, Blocks.OBSIDIAN), (world, pos, cpos, state, cstate) -> {
					world.removeBlock(pos, false);
					if (world instanceof ServerWorld) {
						((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
						world.playSound(null, pos, SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.6F);
						world.playSound(null, pos, Sounds.SPOON_BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
					}
					world.setBlockState(cpos, Jolt.BLOCK.getDefaultState());
				});
		}
	}
	@Override public Item asItem(){ return Items.DISPENSER; };
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new JoltEntity(pos, state); }
}
