package tf.ssf.sfort.operate.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.operate.pipe.util.OperatePipeUpdateS2CPacket;

import java.util.List;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

	@Shadow @Final private ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider;

	@Shadow @Final ChunkPos pos;

	@Inject(method="sendPacketToPlayers(Ljava/util/List;Lnet/minecraft/network/packet/Packet;)V", at=@At("HEAD"), cancellable=true)
	public void operatePacketUnwrap(List<ServerPlayerEntity> players, Packet<?> packet, CallbackInfo ci) {
		if (packet instanceof OperatePipeUpdateS2CPacket) {
			ci.cancel();
			int distance;
			if (!(this.playersWatchingChunkProvider instanceof AccessorThreadedAnvilChunkStorage)) {
				return;
			}
			distance = ((AccessorThreadedAnvilChunkStorage) this.playersWatchingChunkProvider).getWatchDistance();
			BlockEntityUpdateS2CPacket finalPacket = ((OperatePipeUpdateS2CPacket) packet).packet;
			this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.pos, false).forEach(
					spe -> {
						int x1 = this.pos.x;
						int x2 = spe.getWatchedSection().getSectionX();
						int z1 = this.pos.z;
						int z2 = spe.getWatchedSection().getSectionZ();
						if (ThreadedAnvilChunkStorage.isWithinDistance(x1, z1, x2, z2, distance-1)) {
							return;
						}
						BlockPos blockPos = finalPacket.getPos();
						int diff = x1-x2;
						if (diff == distance+1) {
							if ((blockPos.getX() % 16) != 15) return;
						} else if (-diff == distance+1) {
							if ((blockPos.getX() % 16) != 0) return;
						}
						diff = z1 - z2;
						if (diff == distance+1) {
							if ((blockPos.getZ() % 16) != 15) return;
						} else if (-diff == distance+1) {
							if ((blockPos.getZ() % 16) != 0) return;
						}
						spe.networkHandler.sendPacket(finalPacket);
					}
			);
		}
	}


}
