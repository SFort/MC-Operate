package tf.ssf.sfort.operate.pipe.util;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

public class OperatePipeUpdateS2CPacket implements Packet<ClientPlayPacketListener> {
	public final BlockEntityUpdateS2CPacket packet;
	public OperatePipeUpdateS2CPacket(BlockEntityUpdateS2CPacket packet) {
		this.packet = packet;
	}
	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public void apply(ClientPlayPacketListener listener) {
	}
}
