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
import tf.ssf.sfort.operate.pipe.advanced.ExchangePipe;
import tf.ssf.sfort.operate.pipe.FilterPipe;
import tf.ssf.sfort.operate.pipe.advanced.OverseerPipe;
import tf.ssf.sfort.operate.pipe.PriorityPipe;
import tf.ssf.sfort.operate.pipe.advanced.RequestPipe;
import tf.ssf.sfort.operate.pipe.UnloadPipe;
import tf.ssf.sfort.operate.punch.Punch;
import tf.ssf.sfort.operate.stak.BitStak;
import tf.ssf.sfort.operate.stak.cylinder.Cylinder;
import tf.ssf.sfort.operate.stak.cylinder.ItemCylinder;
import tf.ssf.sfort.operate.tube.ColorTube;


public class Main implements ModInitializer {
	public static final DirectionProperty HORIZONTAL_FACING = DirectionProperty.of("facing", (facing) -> facing != Direction.UP && facing != Direction.DOWN);
	public static final ChunkTicketType<BlockPos> PIPE_TICKET_TYPE = ChunkTicketType.create("portal", Vec3i::compareTo, 60);
	public static final Identifier reqPacket = Main.id("req_packet");

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
		ItemCylinder.register();
		Cylinder.register();
		OverseerPipe.register();
		ExchangePipe.register();
		RequestPipe.register();
	}
	public static Identifier id(String name){
		return new Identifier("operate", name);
	}
}
