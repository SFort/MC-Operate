package tf.ssf.sfort.operate;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;


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
		Spoon.SpoonDo.register();
		//BP.register();
	}
	public static Identifier id(String name){
		return new Identifier("operate", name);
	}
}
