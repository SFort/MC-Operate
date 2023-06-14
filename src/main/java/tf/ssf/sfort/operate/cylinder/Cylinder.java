package tf.ssf.sfort.operate.cylinder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.pipe.AbstractPipeEntity;

public class Cylinder extends Block implements BlockEntityProvider {
	public static final IntProperty STAGE = IntProperty.of("stage", 0, 2);
	public static final VoxelShape collisionShape =  Block.createCuboidShape(5.5,0,5.5,10.5,16,10.5);
	public static Block BLOCK;
	public Cylinder() {
		super(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).pistonBehavior(PistonBehavior.NORMAL)
				.strength(2.5F).nonOpaque().luminance(state -> state.get(STAGE)));
		setDefaultState(stateManager.getDefaultState().with(STAGE, 0));
	}
	public Cylinder(Settings settings) {super(settings);}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(STAGE);
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
			if (e instanceof CylinderEntity)
				((CylinderEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		} else {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof CylinderEntity) {
				int trueStage = ((CylinderEntity) e).getStage();
				if (state.get(STAGE) != trueStage) {
					world.setBlockState(pos, state.with(STAGE, trueStage));
					return;
				}
				if (state.get(STAGE) == 0) {
					((CylinderEntity) e).clear();
				}
			}
		}
	}
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (world.isClient) return ActionResult.CONSUME;
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof CylinderEntity) {
			ItemStack handStack = player.getStackInHand(hand);
			switch (state.get(STAGE)) {
				case 1:
					if (handStack.isEmpty() && player.isSneaky()) {
						((CylinderEntity) e).dropInv();
						((CylinderEntity) e).clear();
						world.setBlockState(pos, state.with(STAGE, 0));
						world.playSound(null, pos, Sounds.BIT_CYLINDER, SoundCategory.PLAYERS, 1, .8f + .2f * world.random.nextFloat());
						return ActionResult.SUCCESS;
					}
					break;
				case 2:
					ItemStack stack = ((CylinderEntity) e).createCylinder();
					if (stack != ItemStack.EMPTY) {
						if (handStack.hasNbt()) {
							handStack.getNbt().remove("RepairCost");
						}
						if (handStack.isEmpty()) {
							player.setStackInHand(hand, stack);
						} else if (AbstractPipeEntity.canMergeItems(handStack, stack, handStack.getMaxCount())) {
							handStack.increment(1);
						} else {
							world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack));
						}
						world.setBlockState(pos, state.with(STAGE, 1));
						return ActionResult.SUCCESS;
					}
			}
		}

		return ActionResult.PASS;
	}
	public static void register() {
		if (Config.cylinder == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BLOCK = Registry.register(Registries.BLOCK, Main.id("cylinder"), new Cylinder());
		CylinderEntity.register();
		if (Config.cylinder == Config.EnumOnOffUnregistered.ON) {
			Spoon.CRAFT.put(new Pair<>(Blocks.HOPPER, Blocks.COPPER_BLOCK), (world, pos, cpos, state, cstate) -> {
				world.removeBlock(pos, false);
				if (world instanceof ServerWorld) {
					((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
					world.playSound(null, pos, Sounds.SPOON_BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(cpos, Cylinder.BLOCK.getDefaultState());
				return true;
			});
		}
	}
	@Override public Item asItem(){return Items.LAPIS_BLOCK;}
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new CylinderEntity(pos, state); }


}
