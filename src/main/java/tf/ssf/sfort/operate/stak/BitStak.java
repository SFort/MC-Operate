package tf.ssf.sfort.operate.stak;


import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.util.OperateUtil;
import tf.ssf.sfort.operate.util.StakCompute;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class BitStak extends Block implements BlockEntityProvider{
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final DirectionProperty FACING = Main.HORIZONTAL_FACING;
	public static final Map<Item, Predicate<StakCompute>> VALID_OPS = new HashMap<>();
	public static final Map<Item, Predicate<StakCompute>> VALID_INSNS = new HashMap<>();
	public static final Map<Item, Integer> VALID_CONST = new HashMap<>();
	static {
		VALID_CONST.put(Items.CHARCOAL, -1);
		VALID_CONST.put(Items.GLASS, 0);
		VALID_CONST.put(Items.WHITE_DYE, 1);
		VALID_CONST.put(Items.ORANGE_DYE, 2);
		VALID_CONST.put(Items.MAGENTA_DYE, 3);
		VALID_CONST.put(Items.LIGHT_BLUE_DYE, 4);
		VALID_CONST.put(Items.YELLOW_DYE, 5);
		VALID_CONST.put(Items.LIME_DYE, 6);
		VALID_CONST.put(Items.PINK_DYE, 7);
		VALID_CONST.put(Items.GRAY_DYE, 8);
		VALID_CONST.put(Items.LIGHT_GRAY_DYE, 9);
		VALID_CONST.put(Items.CYAN_DYE, 10);
		VALID_CONST.put(Items.PURPLE_DYE, 11);
		VALID_CONST.put(Items.BLUE_DYE, 12);
		VALID_CONST.put(Items.BROWN_DYE, 13);
		VALID_CONST.put(Items.GREEN_DYE, 14);
		VALID_CONST.put(Items.RED_DYE, 15);
		VALID_CONST.put(Items.BLACK_DYE, 16);
		VALID_CONST.put(Items.WHITE_WOOL, 1);
		VALID_CONST.put(Items.ORANGE_WOOL, 2);
		VALID_CONST.put(Items.MAGENTA_WOOL, 4);
		VALID_CONST.put(Items.LIGHT_BLUE_WOOL, 8);
		VALID_CONST.put(Items.YELLOW_WOOL, 16);
		VALID_CONST.put(Items.LIME_WOOL, 32);
		VALID_CONST.put(Items.PINK_WOOL, 64);
		VALID_CONST.put(Items.GRAY_WOOL, 128);
		VALID_CONST.put(Items.LIGHT_GRAY_WOOL, 256);
		VALID_CONST.put(Items.CYAN_WOOL, 512);
		VALID_CONST.put(Items.PURPLE_WOOL, 1024);
		VALID_CONST.put(Items.BLUE_WOOL, 2048);
		VALID_CONST.put(Items.BROWN_WOOL, 4096);
		VALID_CONST.put(Items.GREEN_WOOL, 8192);
		VALID_CONST.put(Items.RED_WOOL, 16384);
		VALID_CONST.put(Items.BLACK_WOOL, 32768);

		VALID_INSNS.put(Items.REDSTONE, StakCompute::computeAdd);
		VALID_INSNS.put(Items.STICK, StakCompute::computeSub);
		VALID_INSNS.put(Items.IRON_INGOT, StakCompute::computeGreater);
		VALID_INSNS.put(Items.COPPER_INGOT, StakCompute::computeLesser);
		VALID_INSNS.put(Items.LAPIS_LAZULI, StakCompute::computeDiv);
		VALID_INSNS.put(Items.BONE, StakCompute::computeMul);
		VALID_INSNS.put(Items.GLASS_BOTTLE, StakCompute::computeAnd);
		VALID_INSNS.put(Items.FURNACE, StakCompute::computeXor);
		VALID_INSNS.put(Items.REDSTONE_TORCH, StakCompute::computeNot);
		VALID_INSNS.put(Items.TORCH, StakCompute::computeEquals);
		VALID_INSNS.put(Items.COBBLESTONE, StakCompute::computeDup);
		VALID_INSNS.put(Items.GUNPOWDER, StakCompute::computePop);
		VALID_INSNS.put(Items.REPEATER, StakCompute::computeTick);
		VALID_INSNS.put(Items.FEATHER, StakCompute::computeMark);
		VALID_INSNS.put(Items.LEVER, StakCompute::computeJump);
		VALID_INSNS.put(Items.COMPARATOR, StakCompute::computeIf0);
		VALID_INSNS.put(Items.AMETHYST_SHARD, StakCompute::computeSwap);
		VALID_INSNS.put(Items.BOWL, StakCompute::computeStore);
		VALID_INSNS.put(Items.QUARTZ, StakCompute::computeLoad);
		VALID_INSNS.put(Items.SUGAR, StakCompute::computeShiftLeft);
		VALID_INSNS.put(Items.SPIDER_EYE, StakCompute::computeShiftRight);
		VALID_INSNS.put(Items.BLAZE_POWDER, StakCompute::computeColorLoad);
		VALID_INSNS.put(Items.BRICK, StakCompute::computeColorAdd);
		VALID_INSNS.put(Items.FLINT, StakCompute::computeColorSubtract);
		VALID_INSNS.put(Items.PAPER, StakCompute::computeGetColorStrength);
		VALID_INSNS.put(Items.ICE, entity -> true);

		for (Map.Entry<Item, Integer> entry : VALID_CONST.entrySet()) {
			int color = entry.getValue();
			VALID_OPS.put(entry.getKey(), e -> e.computeConst(color));
		}
		VALID_OPS.putAll(VALID_INSNS);
	}

	public static Block BLOCK;
	public BitStak() {
		super(Settings.create().mapColor(MapColor.STONE_GRAY).pistonBehavior(PistonBehavior.BLOCK).strength(1.5F));
		setDefaultState(stateManager.getDefaultState().with(POWERED, false).with(FACING, Direction.NORTH));
	}
	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING).add(POWERED);
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (world.isClient) return null;
		if (type == BitStakEntity.ENTITY_TYPE) return (world1, pos1, state1, entity) -> {
			if (entity instanceof BitStakEntity) {
				((BitStakEntity) entity).serverTick(world1, pos1, state1);
			}
		};
		return null;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos())).with(FACING, ctx.getPlayerLookDirection().getOpposite());
	}
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if (!state.get(POWERED) && world.isReceivingRedstonePower(pos)) {
			world.setBlockState(pos, state.with(POWERED, true));
			BlockEntity entity = world.getBlockEntity(pos);
			if (entity instanceof BitStakEntity) ((BitStakEntity)entity).resetMem();
		}
	}
	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}
	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof BitStakEntity){
			return Math.min(15, ((BitStakEntity)e).redstone);
		}
		return 0;
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof BitStakEntity)
				((BitStakEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		BlockEntity e = world.getBlockEntity(blockPos);
		if (e instanceof BitStakEntity && !blockState.get(POWERED)) {
			ItemStack stack = player.getStackInHand(hand);
			if (VALID_OPS.containsKey(stack.getItem())) {
				if (!world.isClient) {
					((BitStakEntity) e).pushInv(stack.split(1));
					e.markDirty();
				}
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.PASS;
	}
	public BitStak(Settings settings) {super(settings);}
	public static void register() {
		if (Config.bit == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BLOCK = Registry.register(Registries.BLOCK, Main.id("bit"), new BitStak());
		BitStakEntity.register();
		if (Config.bit == Config.EnumOnOffUnregistered.ON) {
			Spoon.SpoonDo craft = (world, pos, cpos, state, cstate) -> {
				Direction dir = OperateUtil.dirFromHorizontalVec(pos.subtract(cpos));
				if (dir == null) return false;
				world.removeBlock(pos, false);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, Sounds.SPOON_BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(cpos, BLOCK.getDefaultState().with(FACING, dir));
				return true;
			};
			Spoon.CRAFT.put(new Pair<>(Blocks.SCULK, Blocks.REDSTONE_BLOCK), craft);
			Spoon.CRAFT.put(new Pair<>(Blocks.REDSTONE_BLOCK, Blocks.SCULK), craft);
		}
	}
	@Override public Item asItem(){return Items.SCULK;}
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new BitStakEntity(pos, state); }
}

