package tf.ssf.sfort.operate.pipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;

public class OverseerPipe extends AbstractPipe{
	public static Block BLOCK;
	public static final DirectionProperty FACING = Properties.FACING;

	public static final VoxelShape[] collisionShape = new VoxelShape[]{
			Block.createCuboidShape(4,0,4,12,12,12),
			Block.createCuboidShape(4,4,4,12,16,12),
			Block.createCuboidShape(4,4,0,12,12,12),
			Block.createCuboidShape(4,4,4,12,12,16),
			Block.createCuboidShape(0,4,4,12,12,12),
			Block.createCuboidShape(4,4,4,16,12,12)
	};

	public OverseerPipe() {
		super(Settings.of(Material.PISTON).strength(1.5F).sounds(Sounds.PIPE_BLOCK_SOUNDS));
		setDefaultState(stateManager.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return collisionShape[state.get(FACING).getId()];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return collisionShape[state.get(FACING).getId()];
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new OverseerPipeEntity(pos, state);
	}

	public static void register() {
		if (Config.advancedPipe == null) return;
		BLOCK = Registry.register(Registry.BLOCK, Main.id("overseer_pipe"), new OverseerPipe());
		OverseerPipeEntity.register();
		if (Config.advancedPipe) {
			Spoon.INFUSE.put(new Pair<>(Items.ENDER_EYE, BasicPipe.BLOCK), (world, pos, state, offhand, context) -> {
				offhand.decrement(1);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, Sounds.SPOON_HIT, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
					world.playSound(null, pos, state.getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(pos, BLOCK.getDefaultState().with(FACING, context.getSide()));
				return ActionResult.SUCCESS;
			});
		}

	}
	@Override public Item asItem(){return Items.ENDER_EYE;}

}
