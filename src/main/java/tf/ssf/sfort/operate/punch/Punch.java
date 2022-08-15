package tf.ssf.sfort.operate.punch;


import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Spoon;

public class Punch extends Block implements BlockEntityProvider{
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static Block BLOCK;
	public Punch() {
		super(Settings.of(Material.PISTON).strength(1.5F));
		setDefaultState(stateManager.getDefaultState().with(POWERED, false));
	}
	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
	}
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		boolean pow = world.isReceivingRedstonePower(pos);
		if (pow != state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, pow));
		}
	}
	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}
	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof PunchEntity){
			return ((PunchEntity)e).getComparator();
		}
		return 0;
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof PunchEntity)
				((PunchEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (!world.isClient) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e instanceof PunchEntity) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack.isEmpty()) ((PunchEntity)e).popInv();
				else ((PunchEntity)e).pushInv(stack.split(1));
				e.markDirty();
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.CONSUME;
	}
	public Punch(Settings settings) {super(settings);}
	public static void register() {
		if (Config.punch != null) {
			BLOCK = Registry.register(Registry.BLOCK, Main.id("punch"), new Punch());
			PunchEntity.register();
			if (Config.punch)
				Spoon.CRAFT.put(new Pair<>(Blocks.PISTON, Blocks.CRAFTING_TABLE), (world, pos, cpos, state, cstate) -> {
					world.removeBlock(pos, false);
					if (world instanceof ServerWorld) {
						((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
						world.playSound(null, pos, Spoon.BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
					}
					world.setBlockState(cpos, Punch.BLOCK.getDefaultState());
				});
		}
	}
	@Override public Item asItem(){return Items.PISTON;}
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new PunchEntity(pos, state); }
}

