package tf.ssf.sfort.operate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NetworkSyncedItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.collection.DefaultedList;
import tf.ssf.sfort.operate.util.StakCompute;

/*
dead code, i don't intend to work on operate any time soon
but am pushing this change since i will still port it.
* */

//Dairy Absorbing Target Acquiring Arrow Leaven(/Launching?) to Enemy Reception Nearly All Terminal Outpuching Rocket
public class DATAALTERNATOR extends NetworkSyncedItem {
	public static Item ITEM;

	public DATAALTERNATOR() {
		super(new Settings().maxCount(1));
	}

	public DATAALTERNATOR(Settings settings) {
		super(settings);
	}

	public static void register() {
		if (Config.cylinder == Config.EnumOnOffUnregistered.UNREGISTERED) return;
		ITEM = Registry.register(Registries.ITEM, Main.id("dataalternator"), new DATAALTERNATOR());
	}
	@Override
	public ItemStack getDefaultStack() {
		return new ItemStack(this);
	}
	@Override
	public boolean isNbtSynced() {
		return false;
	}

	/*public static class DATAALTERNATORCompute implements StakCompute {

	}*/
}
