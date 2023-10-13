package tf.ssf.sfort.operate.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.util.PacketBufCustomPayload;

@Mixin(CustomPayloadS2CPacket.class)
public class OldPacketClientMixin {

	@Inject(method="readPayload(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/CustomPayload;", at=@At("HEAD"), cancellable=true)
	private static void oldPayload(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<CustomPayload> cir){
		if (Main.reqPacket.getNamespace().equals(id.getNamespace())) {
			cir.setReturnValue(new PacketBufCustomPayload(buf));
		}
	}
}
