package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import tf.ssf.sfort.operate.Config;

public class EntrancePipeRenderer<T extends EntrancePipeEntity> extends AbstractPipeRenderer<T> {

	public static void register() {
		if (Config.basicPipe == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		BlockEntityRendererRegistry.register(EntrancePipeEntity.ENTITY_TYPE, ctx -> new EntrancePipeRenderer<>()::render);
	}

}
