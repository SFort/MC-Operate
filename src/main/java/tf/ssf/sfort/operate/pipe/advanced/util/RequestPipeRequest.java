package tf.ssf.sfort.operate.pipe.advanced.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class RequestPipeRequest implements Predicate<ItemStack> {
	public static final RequestPipeRequest ALL = new RequestPipeRequest(Items.AIR, 0, 0){
		@Override
		public boolean test(ItemStack stack) {
			return true;
		}
		@Override
		public boolean subtract(int count) {
			return false;
		}
		@Override
		public void writeNbt(NbtCompound tag) {
			tag.putByte("ALL", (byte) 1);
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
	public static RequestPipeRequest fromNbt(NbtCompound tag) {
		if (tag == null || tag.isEmpty()) return null;
		if (tag.contains("ALL")) return ALL;
		RequestPipeRequest ret = new RequestPipeRequest(tag);
		RequestPipeRequest last = ret;
		for (tag = tag.getCompound("next"); !tag.isEmpty(); tag = tag.getCompound("next")) {
			last = last.next = new RequestPipeRequest(tag);
		}
		return ret;
	}
	RequestPipeRequest(NbtCompound nbt) {
		this.item = Registries.ITEM.get(new Identifier(nbt.getString("item")));
		this.hash = nbt.getInt("hash");
		this.count = nbt.getInt("count");
	}
	public void writeNbt(NbtCompound tag) {
		tag.putString("item", Registries.ITEM.getId(this.item).toString());
		tag.putInt("hash", this.hash);
		tag.putInt("count", this.count);
		for (RequestPipeRequest next = this.next; next != null; next=next.next) {
			NbtCompound nextTag = new NbtCompound();
			nextTag.putString("item", Registries.ITEM.getId(this.item).toString());
			nextTag.putInt("hash", this.hash);
			nextTag.putInt("count", this.count);
			tag.put("next", nextTag);
			tag = nextTag;
		}
	}
	public NbtCompound toNbt(NbtCompound tag) {
		writeNbt(tag);
		return tag;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (stack.isOf(item)) {
			if (hash == 0) {
				return !stack.hasNbt();
			} else {
				NbtCompound nbt = stack.getNbt();
				return nbt != null && nbt.hashCode() == hash;
			}
		}
		return false;
	}

	public boolean subtract(int count) {
		this.count -= count;
		return this.count<=0;
	}
}
