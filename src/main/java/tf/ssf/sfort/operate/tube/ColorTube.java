package tf.ssf.sfort.operate.tube;


import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.Spoonable;

public class ColorTube extends Block implements BlockEntityProvider, Spoonable {
	public static final BooleanProperty ENABLED = Properties.ENABLED;
	public static final VoxelShape collisionShape =  Block.createCuboidShape(4,4,4,12,12,12);
	public static Block BLOCK;
	public ColorTube() {
		super(Settings.of(Material.PISTON).nonOpaque().strength(.5f).sounds(Sounds.PIPE_BLOCK_SOUNDS));
		setDefaultState(stateManager.getDefaultState().with(ENABLED, false));
	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ENABLED);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return collisionShape;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return collisionShape;
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if (world.isAir(fromPos)) {
			BlockEntity entity = world.getBlockEntity(pos);
			if (entity instanceof ColorTubeEntity) {
				Direction direction = Direction.fromVector(fromPos.subtract(pos));
				if (direction != null) {
					boolean enabled = ((ColorTubeEntity) entity).setColor(direction, TubeConnectTypes.NONE);
					if (enabled != state.get(ENABLED)) world.setBlockState(pos, state.with(ENABLED, enabled));
				}
			}
		}
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult operate$onUse(BlockState blockState, World world, BlockPos blockPos, ItemUsageContext blockHitResult) {
		if (!world.isClient) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e instanceof ColorTubeEntity && blockState.isOf(BLOCK)) {
				Direction dir = blockHitResult.getSide();
				boolean enabled = ((ColorTubeEntity)e).wrenchSide(dir);
				if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
				blockPos = blockPos.offset(dir);
				e = world.getBlockEntity(blockPos);
				blockState = world.getBlockState(blockPos);
				PlayerEntity player = blockHitResult.getPlayer();
				if (e instanceof ColorTubeEntity && blockState.isOf(BLOCK)  && player != null && !player.isSneaky()) {
					enabled = ((ColorTubeEntity)e).wrenchSide(dir.getOpposite());
					if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
				}
				return ActionResult.SUCCESS;
			}
		}
		return null;
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (!world.isClient && blockState.isOf(BLOCK)) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e instanceof ColorTubeEntity) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack !=null) {
					TubeConnectTypes clr = TubeConnectTypes.itemMap.get(stack.getItem());
					if (clr != null) {
						Direction dir = blockHitResult.getSide();
						boolean enabled = ((ColorTubeEntity)e).setColor(dir, clr);
						if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
						blockPos = blockPos.offset(dir);
						e = world.getBlockEntity(blockPos);
						blockState = world.getBlockState(blockPos);
						if (e instanceof ColorTubeEntity && blockState.isOf(BLOCK)) {
							enabled = ((ColorTubeEntity) e).setColor(dir.getOpposite(), clr);
							if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
						}
						return ActionResult.SUCCESS;
					}
				}
			}
		}
		return ActionResult.PASS;
	}
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof ColorTubeEntity) return ((ColorTubeEntity)e).getWeakPower(direction.getOpposite());
		return 0;
	}
	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return state.get(ENABLED);
	}
	public ColorTube(Settings settings) {super(settings);}
	public static void register() {
		if (Config.colorTube == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BLOCK = Registry.register(Registries.BLOCK, Main.id("color_tube"), new ColorTube());
		ColorTubeEntity.register();
		if (Config.colorTube == Config.EnumOnOffUnregistered.ON) {
			Spoon.PLACE.put(Items.REDSTONE, (world, pos, state, offhand, side) -> {
				BlockPos gpos = pos.offset(side);
				if (world.getBlockState(gpos).isAir()) {
					offhand.decrement(1);
					world.setBlockState(gpos, ColorTube.BLOCK.getDefaultState());
					BlockEntity e = world.getBlockEntity(gpos);
					if (e instanceof ColorTubeEntity) ((ColorTubeEntity)e).justPlaced();
					return ActionResult.SUCCESS;
				}
				return null;
			});
		}

	}
	@Override public Item asItem(){return Items.REDSTONE;}
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new ColorTubeEntity(pos, state); }

}

