package tf.ssf.sfort.operate;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.operate.jolt.Jolt;
import tf.ssf.sfort.operate.pipe.EntrancePipe;
import tf.ssf.sfort.operate.punch.Punch;
import tf.ssf.sfort.operate.stak.BitStak;
import tf.ssf.sfort.operate.tube.ColorTube;


public class Main implements ModInitializer {
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
		EntrancePipe.register();
		//BP.register();
	}
	public static Identifier id(String name){
		return new Identifier("operate", name);
	}
}
