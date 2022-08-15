package tf.ssf.sfort.operate.pipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.tube.ColorTubeEntity;

public class EntrancePipe extends AbstractPipe{
	public static Block BLOCK;

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EntrancePipeEntity(pos, state);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof EntrancePipeEntity) {
			if (entity instanceof ItemEntity) {
				ItemStack stack = ((ItemEntity) entity).getStack();
				if (entity.isAlive()) {
					entity.kill();
					Direction dir;
					Vec3d epos = entity.getPos();
					double x = pos.getX() - epos.x;
					double y = pos.getY() - epos.y;
					double z = pos.getZ() - epos.z;
					if (x>y){
						if (x>z){
							if (x<0) dir = Direction.WEST;
							else dir = Direction.EAST;
						} else {
							if (z>0) dir = Direction.SOUTH;
							else dir = Direction.NORTH;
						}
					} else if (z>y) {
						if (z>0) dir = Direction.SOUTH;
						else dir = Direction.NORTH;
					} else if (y<0) {
						dir = Direction.DOWN;
					} else {
						dir = Direction.UP;
					}
					((EntrancePipeEntity) be).pushNewItem(stack, dir);
				}
			}
		}
	}

	public static void register() {
		if (false) return;
		BLOCK = Registry.register(Registry.BLOCK, Main.id("entrance_pipe"), new EntrancePipe());
		EntrancePipeEntity.register();
		if (true) {
			Spoon.PLACE.put(Items.IRON_INGOT, (context -> {
				World world = context.getWorld();
				PlayerEntity p = context.getPlayer();
				BlockPos gpos = context.getBlockPos().offset(context.getSide());
				if (p != null && world.getBlockState(gpos).isAir()) {
					p.getOffHandStack().decrement(1);
					world.setBlockState(gpos, EntrancePipe.BLOCK.getDefaultState());
					BlockEntity e = world.getBlockEntity(gpos);
					if (e instanceof ColorTubeEntity) ((ColorTubeEntity) e).justPlaced();
					return true;
				}
				return false;
			}));
		}

	}
	@Override public Item asItem(){return Items.IRON_INGOT;}

}
