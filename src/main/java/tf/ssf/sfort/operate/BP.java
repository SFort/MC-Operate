package tf.ssf.sfort.operate;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.client.BPScreen;
import tf.ssf.sfort.operate.client.McClient;

public class BP extends Item {
	public static Item ITEM;

	public static void register() {
		ITEM = Registry.register(Registry.ITEM, Main.id("blue_print"), new BP());
	}
	public BP() {
		super(new Settings().group(ItemGroup.TOOLS));
	}
	public BP(Settings settings) {
		super(settings);
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if(world.isClient){
			McClient.mc.mouse.unlockCursor();
			McClient.mc.currentScreen=new BPScreen(user.getStackInHand(hand).getName());
			McClient.mc.currentScreen.init(McClient.mc, McClient.mc.getWindow().getScaledWidth(), McClient.mc.getWindow().getScaledHeight());
		}
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
}