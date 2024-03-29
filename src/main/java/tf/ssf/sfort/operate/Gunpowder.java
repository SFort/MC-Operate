package tf.ssf.sfort.operate;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class Gunpowder extends HorizontalConnectingBlock {
	public static final int time = 7;
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
	public static Block BLOCK;
	public Gunpowder(Settings settings) {
		super(8.0F, 0.0F, 1.0F, 1.0F, 1.0F, settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(TRIGGERED, false).with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(WATERLOGGED, false));
	}
	public static void register(){
		if (Config.gunpowder == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BLOCK = Registry.register(Registries.BLOCK, new Identifier("operate", "gunpowder"), new Gunpowder(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).pistonBehavior(PistonBehavior.DESTROY).noCollision().breakInstantly()));
		if (Config.gunpowder == Config.EnumOnOffUnregistered.ON) {
			Spoon.PLACE.put(Items.GUNPOWDER, (world, pos, state, offhand, side) -> {
				BlockPos gpos = pos.offset(side);
				if (world.getBlockState(gpos.down()).isSideSolidFullSquare(world, gpos.down(), side) && world.getBlockState(gpos).isAir()) {
					offhand.decrement(1);
					world.setBlockState(gpos, ((Gunpowder) Gunpowder.BLOCK).getPlacementState(world, gpos));
					return ActionResult.SUCCESS;
				}
				return null;
			});
		}
	}
	@Override
	public Item asItem(){ return Items.GUNPOWDER; };
	public boolean emitsRedstonePower(BlockState state) { return true; }
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) { return state.get(TRIGGERED)?15:0; }

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if(state.get(TRIGGERED))
			world.removeBlock(pos, false);
		else {
			world.setBlockState(pos, state.with(TRIGGERED, true));
			for (Direction dir : Direction.Type.HORIZONTAL)
				if (world.getBlockState(pos.up().offset(dir)).getBlock() instanceof Gunpowder)
					world.scheduleBlockTick(pos.up().offset(dir), this, time);

			world.updateNeighbors(pos.down(), this);
			world.scheduleBlockTick(pos, this, time);
		}
	}
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack itemStack = player.getStackInHand(hand);
		Item item = itemStack.getItem();
		if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
			if (!player.isCreative()) {
				if (item == Items.FLINT_AND_STEEL) {
					itemStack.damage(1, player, (playerEntity) -> {
						playerEntity.sendToolBreakStatus(hand);
					});
				} else {
					itemStack.decrement(1);
				}
			}
			world.scheduleBlockTick(pos, this, time);

			return ActionResult.success(world.isClient);
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		if (!world.isClient && projectile.isOnFire())
			world.scheduleBlockTick(hit.getBlockPos(), this, time);

	}
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getPlacementState(ctx.getWorld(), ctx.getBlockPos());
	}
	public BlockState getPlacementState(World world, BlockPos pos) {
		boolean pow = world.isReceivingRedstonePower(pos);
		if(pow)
			world.scheduleBlockTick(pos, this, time);
		return this.getDefaultState()
				.with(TRIGGERED, pow)
				.with(NORTH, !world.getBlockState(pos.north()).isReplaceable())
				.with(SOUTH, !world.getBlockState(pos.south()).isReplaceable())
				.with(WEST, !world.getBlockState(pos.west()).isReplaceable())
				.with(EAST, !world.getBlockState(pos.east()).isReplaceable());
	}

	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if(fromPos.getY()<pos.getY()){
			if (!world.getBlockState(fromPos).isFullCube(world,fromPos)) {
				world.breakBlock(pos, true);
				return;
			}
		} else {
			if (world.getBlockState(fromPos).getBlock() instanceof FireBlock) {
				if (!state.get(TRIGGERED)) {
					world.scheduleBlockTick(pos, this, time);
				}
				return;
			}
		}
		if (world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up())) {
			if (!state.get(TRIGGERED)) {
				world.scheduleBlockTick(pos, this, time);
			}
		}
	}
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		return direction.getAxis().isHorizontal()?
				state.with(FACING_PROPERTIES.get(direction), !newState.isReplaceable())
				:super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED, TRIGGERED);
	}
	public void rainTick(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if(state.get(TRIGGERED))
			world.setBlockState(pos,state.with(TRIGGERED, false),3);
	}
}
