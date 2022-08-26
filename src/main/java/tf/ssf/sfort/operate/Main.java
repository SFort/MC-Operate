package tf.ssf.sfort.operate;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import tf.ssf.sfort.operate.jolt.Jolt;
import tf.ssf.sfort.operate.pipe.BasicPipe;
import tf.ssf.sfort.operate.pipe.EntrancePipe;
import tf.ssf.sfort.operate.pipe.FilterPipe;
import tf.ssf.sfort.operate.pipe.PriorityPipe;
import tf.ssf.sfort.operate.pipe.UnloadPipe;
import tf.ssf.sfort.operate.punch.Punch;
import tf.ssf.sfort.operate.stak.BitStak;
import tf.ssf.sfort.operate.tube.ColorTube;


public class Main implements ModInitializer {
	public static final DirectionProperty HORIZONTAL_FACING = DirectionProperty.of("facing", (facing) -> facing != Direction.UP && facing != Direction.DOWN);
	public static final ChunkTicketType<BlockPos> PIPE_TICKET_TYPE = ChunkTicketType.create("portal", Vec3i::compareTo, 60);


	@Override
	public void onInitialize() {
		Config.load();
		Gunpowder.register();
		Jolt.register();
		Punch.register();
		ObsidianDispenser.registerPowder();
		ObsidianDispenser.register();
		Spoon.register();
		BitStak.register();
		ColorTube.register();
		BasicPipe.register();
		EntrancePipe.register();
		UnloadPipe.register();
		PriorityPipe.register();
		FilterPipe.register();
		Sounds.register();

		//BP.register();
	}
	public static Identifier id(String name){
		return new Identifier("operate", name);
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
