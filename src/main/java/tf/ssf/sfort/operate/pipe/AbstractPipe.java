package tf.ssf.sfort.operate.pipe;


import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoonable;
import net.minecraft.block.piston.PistonBehavior;

public abstract class AbstractPipe extends Block implements BlockEntityProvider, Spoonable {
	public static final VoxelShape collisionShape =  Block.createCuboidShape(4,4,4,12,12,12);
	public AbstractPipe() {
		super(Settings.create().mapColor(MapColor.STONE_GRAY).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().strength(.35f).sounds(Sounds.PIPE_BLOCK_SOUNDS));
	}
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (!world.isClient) return null;
		//Scheduled ticks are a nope client-side, thanks mojang
		return (world1, pos, state1, blockEntity) -> {
			if (blockEntity instanceof AbstractPipeEntity) {
				((AbstractPipeEntity) blockEntity).pipeTick();
			}
		};
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
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof AbstractPipeEntity)
				((AbstractPipeEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		BlockEntity e = world.getBlockEntity(pos);
		if(e instanceof AbstractPipeEntity) {
			((AbstractPipeEntity) e).pipeTick();
		}
	}
	@Override
	public ActionResult operate$onUse(BlockState blockState, World world, BlockPos blockPos, ItemUsageContext blockHitResult) {
		if (!world.isClient) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e instanceof AbstractPipeEntity) {
				Direction dir = blockHitResult.getSide();
				((AbstractPipeEntity)e).wrenchSide(dir);
				PlayerEntity player = blockHitResult.getPlayer();
				if (player != null && !player.isSneaky()) {
					((AbstractPipeEntity)e).wrenchNeighbour(dir);
				}
				return ActionResult.SUCCESS;
			}
		}
		return null;
	}
	public AbstractPipe(Settings settings) {super(settings);}

	@Override
	abstract public BlockEntity createBlockEntity(BlockPos pos, BlockState state);

}

