package tf.ssf.sfort;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;


public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        //Gunpowder.register();
        Jolt.register();
        ObsidianDispenser.register();
        Spoon.register();
    }
    public static Identifier id(String name){
        return new Identifier("operate", name);
    }
}
