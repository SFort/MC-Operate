package tf.ssf.sfort.operate.client;

import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.advanced.RequestPipe;
import tf.ssf.sfort.operate.pipe.advanced.RequestPipeEntity;

import java.util.function.Consumer;

import static tf.ssf.sfort.operate.MainClient.mc;

public class FakeRequestScreen extends Screen {
	public boolean closed = false;
	public FakeRequestScreen() {
		super(Text.literal("Fake screen"));
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		fail: {
			BlockPos pos = getReqPos();
			if (pos == null) break fail;
			if (mc.world == null) break fail;
			BlockEntity state = mc.world.getBlockEntity(pos);
			if (!(state instanceof RequestPipeEntity)) break fail;
			if (((RequestPipeEntity) state).rpui == null) break fail;
			Consumer<Packet<?>> sender = getSender();
			if (sender == null) break fail;
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos);
			buf.writeByte(0);
			buf.writeChar(chr);
			sender.accept(new CustomPayloadC2SPacket(Main.reqPacket, buf));
			return true;
		}
		close();
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			fail: {
				BlockPos pos = getReqPos();
				if (pos == null) break fail;
				if (mc.world == null) break fail;
				BlockEntity state = mc.world.getBlockEntity(pos);
				if (!(state instanceof RequestPipeEntity)) break fail;
				if (((RequestPipeEntity) state).rpui == null) break fail;
				Consumer<Packet<?>> sender = getSender();
				if (sender == null) break fail;
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer()).writeBlockPos(pos);
				buf.writeVarInt(1);
				sender.accept(new CustomPayloadC2SPacket(Main.reqPacket, buf));
				return true;
			}
			close();
			return false;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void close() {
		closed = true;
	}

	public FakeRequestScreen getSelf() {
		if (closed) return null;
		if (getReqPos() == null) {
			close();
			return null;
		}
		return this;
	}
	public Consumer<Packet<?>> getSender() {
		if (mc == null) return null;
		ClientPlayNetworkHandler net = mc.getNetworkHandler();
		if (net == null) return null;
		return net::sendPacket;
	}
	public BlockPos getReqPos() {
		if (mc == null || mc.world == null) return null;
		HitResult hit = mc.crosshairTarget;
		if (!(hit instanceof BlockHitResult)) return null;
		BlockPos pos = ((BlockHitResult) hit).getBlockPos();
		BlockState state = mc.world.getBlockState(pos);
		if (state != null && state.isOf(RequestPipe.BLOCK)) return pos;
		return null;
	}
}
