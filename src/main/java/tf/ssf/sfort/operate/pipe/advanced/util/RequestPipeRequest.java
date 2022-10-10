package tf.ssf.sfort.operate.pipe.advanced.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.function.Predicate;

public class RequestPipeRequest implements Predicate<ItemStack> {
	public static final RequestPipeRequest ALL = new RequestPipeRequest(Items.AIR, 0, 0){
		@Override
		public boolean test(ItemStack stack) {
			return true;
		}
	};

	public final Item item;
	public final int hash;
	public int count;
	public RequestPipeRequest next = null;

	RequestPipeRequest(Item item, int hash, int count) {
		this.item = item;
		this.hash = hash;
		this.count = count;
	}

	@Override
	public boolean test(ItemStack stack) {
		return stack.isOf(item) && (hash == 0 ? !stack.hasNbt() : stack.hasNbt() && stack.getNbt().hashCode() == hash);
	}
}
