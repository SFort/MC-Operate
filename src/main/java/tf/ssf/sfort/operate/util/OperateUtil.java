package tf.ssf.sfort.operate.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiPredicate;

public class OperateUtil {
	public static<T extends BlockEntity> LinkedHashSet<T> getConnecting(World world, BlockPos pos, Class<T> clazz, BiPredicate<T, Direction> isConnected) {
		LinkedHashSet<T> entities = new LinkedHashSet<>();
		List<T> reader = new ArrayList<>();
		BlockPos.Mutable mut = pos.mutableCopy();
		for (Direction dir : Direction.values()) {
			BlockEntity e = world.getBlockEntity(mut.set(pos).move(dir));
			if (clazz.isInstance(e) && isConnected.test((T)e, dir.getOpposite())) {
				reader.add((T)e);
				entities.add((T)e);
			}
		}
		return getConnecting(world, clazz, isConnected, entities, reader, new ArrayList<>(), mut);
	}
	public static<T extends BlockEntity> LinkedHashSet<T> getConnecting(World world, BlockPos pos, Class<T> clazz, BiPredicate<T, Direction> isConnected, T self) {
		LinkedHashSet<T> entities = new LinkedHashSet<>();
		List<T> reader = new ArrayList<>();
		BlockPos.Mutable mut = pos.mutableCopy();
		for (Direction dir : Direction.values()) {
			if (self != null && !isConnected.test(self, dir)) continue;
			BlockEntity e = world.getBlockEntity(mut.set(pos).move(dir));
			if (clazz.isInstance(e) && isConnected.test((T)e, dir.getOpposite())) {
				reader.add((T)e);
				entities.add((T)e);
			}
		}
		return getConnecting(world, clazz, isConnected, entities, reader, new ArrayList<>(), mut);
	}
	public static<T extends BlockEntity> LinkedHashSet<T> getConnecting(World world, Class<T> clazz, BiPredicate<T, Direction> isConnected, LinkedHashSet<T> entities, List<T> reader, List<T> writer, BlockPos.Mutable mut) {
		do {
			for (T e : reader) {
				for (Direction dir : Direction.values()) {
					if (isConnected.test(e, dir)) {
						BlockEntity neighbour = world.getBlockEntity(mut.set(e.getPos()).move(dir));
						if (clazz.isInstance(neighbour) && isConnected.test((T)neighbour, dir.getOpposite())) {
							if (entities.add((T)neighbour)) {
								writer.add((T)neighbour);
							}
						}
					}
				}
			}
			reader.clear();
			List<T> swp = reader;
			reader = writer;
			writer = swp;
		} while (!reader.isEmpty());
		return entities;
	}

	public static Direction dirFromVec(double x, double y, double z) {
		if (y<0) return Direction.DOWN;
		if (y>=.75) return Direction.UP;
		if (x<.1) return Direction.WEST;
		if (x>=.75) return Direction.EAST;
		if (z<.1) return Direction.NORTH;
		return Direction.SOUTH;
	}

	public static Direction dirFromHorizontalVec(Vec3i vec) {
		int x = Integer.compare(vec.getX(), 0);
		int z = x == 0 ? vec.getZ() : 0;
		if (x != 0 || z != 0) {
			return Direction.fromVector(x, 0, z);
		}
		return null;
	}
}
