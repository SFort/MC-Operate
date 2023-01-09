package tf.ssf.sfort.operate.pipe.advanced;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.MainClient;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.pipe.AbstractPipe;
import tf.ssf.sfort.operate.pipe.BasicPipe;

public class RequestPipe extends AbstractPipe {
	public static Block BLOCK;

	public static VoxelShape[] panelOutlineShapes = new VoxelShape[] {
			VoxelShapes.union(Block.createCuboidShape(0, 0, 0, 16, 16, .1), AbstractPipe.collisionShape),
			VoxelShapes.union(Block.createCuboidShape(15.9, 0, 0, 16, 16, 16), AbstractPipe.collisionShape),
			VoxelShapes.union(Block.createCuboidShape(0, 0, 15.9, 16, 16, 16), AbstractPipe.collisionShape),
			VoxelShapes.union(Block.createCuboidShape(0, 0, 0, .1, 16, 16), AbstractPipe.collisionShape)
	};

	public RequestPipe() {
		super(Settings.of(Material.PISTON).strength(1.5F));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new RequestPipeEntity(pos, state);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof RequestPipeEntity && ((RequestPipeEntity) be).rpui != null) {
			int fac = MainClient.getHorizontalPlayerFacing();
			if (fac != -1)
				return panelOutlineShapes[fac];
		}
		return super.getOutlineShape(state, world, pos, context);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			MainClient.requestPipeKeyboardHack();
			return ActionResult.SUCCESS;
		}
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof RequestPipeEntity) {
			if (((RequestPipeEntity) be).isBusy()) return ActionResult.CONSUME;
			ItemStack stack = player.getStackInHand(hand);
			if (stack.isOf(Items.LAVA_BUCKET)) {
				stack.decrement(1);
				player.giveItemStack(Items.BUCKET.getDefaultStack());
				((RequestPipeEntity) be).requestAll();
			} else if (stack.isEmpty()) {
				Vec3d hitPos = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
				Direction hitDir = hit.getSide();
				((RequestPipeEntity) be).playerInteraction(
						switch (hitDir) {
							case DOWN, UP -> 0;
							case NORTH -> hitPos.x;
							case SOUTH -> 1-hitPos.x;
							case WEST -> 1-hitPos.z;
							case EAST -> hitPos.z;
						}
						, hitPos.y, player.getHorizontalFacing().getOpposite() == hitDir);
			}
		}
		return ActionResult.CONSUME;
	}

	public static void register() {
		if (Config.advancedPipe == null) return;
		BLOCK = Registry.register(Registries.BLOCK, Main.id("request_pipe"), new RequestPipe());
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
