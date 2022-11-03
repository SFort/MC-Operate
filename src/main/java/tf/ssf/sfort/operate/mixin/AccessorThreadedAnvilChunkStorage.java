package tf.ssf.sfort.operate.mixin;

import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface AccessorThreadedAnvilChunkStorage {

	@Accessor("watchDistance")
	int getWatchDistance();
}
