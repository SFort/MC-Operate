package tf.ssf.sfort.operate.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.advanced.RequestPipeEntity;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
	@Shadow public ServerPlayerEntity player;

	@Inject(method="onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", at=@At("HEAD"), cancellable=true)
	public void handleOperatePackets(CustomPayloadC2SPacket packet, CallbackInfo ci){
		if (packet.getChannel().equals(Main.reqPacket)) {
			NetworkThreadUtils.forceMainThread(packet, (ServerPlayNetworkHandler)(Object)this, player.getServerWorld());
			PacketByteBuf buf = packet.getData();
			BlockPos pos = buf.readBlockPos();
			if (player.getBlockPos().isWithinDistance(pos, 10)) {
				player.swingHand(player.getActiveHand(), true);
				BlockEntity be = player.getWorld().getBlockEntity(pos);
				if (be instanceof RequestPipeEntity) {
					switch (buf.readByte()) {
						case 0:
							((RequestPipeEntity) be).playerType(buf.readChar());
							break;
						case 1:
							((RequestPipeEntity) be).playerClearFilter();
					}
				}
			}
			ci.cancel();
		}
	}
}
