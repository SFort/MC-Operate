package tf.ssf.sfort.operate.pipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;

public class UnloadPipe extends AbstractPipe{
	public static Block BLOCK;
	public static final DirectionProperty FACING = Main.HORIZONTAL_FACING;

	public static final VoxelShape collisionShape =  Block.createCuboidShape(4,4,.1,12,12,12);
	public static final VoxelShape outlineShape =  Block.createCuboidShape(4,4, 0,12,12,12);

	public UnloadPipe() {
		super(Settings.of(Material.PISTON).strength(1.5F).sounds(Sounds.PIPE_BLOCK_SOUNDS));
		setDefaultState(stateManager.getDefaultState().with(FACING, Direction.NORTH));
	}
	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity instanceof Inventory && entity.isAlive()) {
			Direction dir = state.get(FACING);
			{
				if (dir != Main.dirFromHorizontalVec(entity.getBlockPos().subtract(pos)))
					return;
			}
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof UnloadPipeEntity) {
				if (((Inventory) entity).isEmpty()){
					if (world instanceof ServerWorld) {
						world.playSound(null, pos, Sounds.LOADER_LOCK, SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
					}
					entity.addVelocity(dir.getOffsetX(),0, dir.getOffsetZ());
					return;
				}
				for (int i=0, size=((Inventory) entity).size(); i<size; i++) {
					ItemStack stack = ((Inventory) entity).removeStack(i);
					if (stack != null && !stack.isEmpty()) {
						if (!((UnloadPipeEntity) be).acceptItemFrom(stack, dir)) {
							((Inventory) entity).setStack(i, stack);
							entity.addVelocity(dir.getOffsetX(),0, dir.getOffsetZ());
							if (world instanceof ServerWorld) {
								world.playSound(null, pos, Sounds.LOADER_LOCK, SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
							}
						}
						return;
					}
				}
			}

		}
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
		return outlineShape;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return collisionShape;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new UnloadPipeEntity(pos, state);
	}

	public static void register() {
		if (false) return;
		BLOCK = Registry.register(Registry.BLOCK, Main.id("unloading_pipe"), new UnloadPipe());
		UnloadPipeEntity.register();
		if (true) {
			Spoon.CRAFT.put(new Pair<>(Blocks.HOPPER, Blocks.STONE), (world, pos, cpos, state, cstate) -> {
				world.removeBlock(pos, false);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, Sounds.SPOON_BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				state = BLOCK.getDefaultState();
				Direction dir = Main.dirFromHorizontalVec(pos.subtract(cpos));
				if (dir != null) {
					state = state.with(FACING, dir);
				}
				world.setBlockState(cpos, state);
			});
		}

	}
	@Override public Item asItem(){return Items.IRON_INGOT;}

}