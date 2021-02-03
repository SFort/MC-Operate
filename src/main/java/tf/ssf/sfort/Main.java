package tf.ssf.sfort;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;


public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        //Registry.register(Registry.BLOCK,new Identifier("operate","gunpowder"),new Gunpowder(AbstractBlock.Settings.of(Material.SUPPORTED).noCollision().breakInstantly()));
        ObsidianDispenser.register();
        Spoon.register();
    }
    public static Identifier id(String name){
        return new Identifier("operate", name);
    }
}
