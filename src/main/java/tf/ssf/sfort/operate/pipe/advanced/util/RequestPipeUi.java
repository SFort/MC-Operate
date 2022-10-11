package tf.ssf.sfort.operate.pipe.advanced.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class RequestPipeUi {
	public final ArrayList<Map.Entry<Key, Data>> items;
	public final Map<Key, Data> reqMap = new HashMap<>();
	public int selectedItem = -1;
	int startIndex = 0;
	int emptySlots = 0;
	public RequestPipeUi(NbtCompound nbt){
		SortedMap<Key, Data> map = new TreeMap<>(Comparator.comparing(i -> Registry.ITEM.getId(i.item)));
		selectedItem = nbt.getInt("selected");
		int i = 1;
		for (NbtCompound tag = nbt.getCompound("0"); !tag.isEmpty(); tag = nbt.getCompound(Integer.toString(i++))){
			map.put(new Key(tag), Data.fromNbt(tag));
		}
		items = new ArrayList<>(map.entrySet());
	}
	public RequestPipeUi(World world, RequestPipeCache cache){
		SortedMap<Key, Data> map = new TreeMap<>(Comparator.comparing(i -> Registry.ITEM.getId(i.item)));
		cache.firstEntry(world, node -> {
			Inventory inv = node.getInv();
			if (inv == null) return false;
			for (int i=0, size=inv.size(); i<size; i++) {
				ItemStack stack = inv.getStack(i);
				if (stack.isEmpty()) {
					emptySlots++;
					continue;
				}
				Key key = new Key(stack.getItem(), stack.hasNbt() ? stack.getNbt() : null);
				Data oldValue = map.get(key);
				if (oldValue == null){
					map.put(key, new Data(stack.getCount(), new HashSet<>(Set.of(node))));
				} else {
					oldValue.count+=stack.getCount();
					oldValue.cachePos.add(node);
				}
			}
			return false;
		});
		items = new ArrayList<>(map.entrySet());
	}
	public static RequestPipeUi fromNbt(NbtCompound tag) {
		if (tag == null || tag.isEmpty()) return null;
		return new RequestPipeUi(tag);
	}
	public void writeNbt(NbtCompound tag) {
		tag.putInt("selected", selectedItem - startIndex);
		for (int i=0, size=items.size(); i+startIndex<size && i<20; i++) {
			Map.Entry<Key, Data> entry = items.get(i+startIndex);
			tag.put(Integer.toString(i), entry.getValue().toNbt(entry.getKey().toNbt(new NbtCompound())));
		}
	}
	public NbtCompound toNbt(NbtCompound tag) {
		writeNbt(tag);
		return tag;
	}

	public boolean playerClickedSlot(int i) {
		if (i<20) {
			i = MathHelper.clamp(i+startIndex, startIndex, Math.min(startIndex+19, items.size()));
			selectedItem = i;
			Map.Entry<Key, Data> entry = items.get(i);
			entry.getValue().reqCount++;
			reqMap.putIfAbsent(entry.getKey(), entry.getValue());
			return true;
		}
		switch (i) {
			case 20:
				if (startIndex+20 < items.size()) startIndex+=20;
				break;
			case 21:
				return false;
			case 22:
				//TODO open submenu for item count
				break;
			case 23:
				reqMap.clear();
				return false;
			case 24:
				if (startIndex > 0) startIndex=Math.max(0, startIndex-20);
				break;
		}
		return true;
	}

	public RequestPipeRequest generateRequests() {
		if (reqMap.isEmpty()) return null;
		Iterator<Map.Entry<Key, Data>> iter = reqMap.entrySet().iterator();
		if (!iter.hasNext()) return null;
		Map.Entry<Key, Data> entry = iter.next();
		Key key = entry.getKey();
		RequestPipeRequest ret = new RequestPipeRequest(key.item, key.nbt == null || key.nbt.isEmpty() ? 0 : key.nbt.hashCode(), entry.getValue().reqCount);
		RequestPipeRequest latest = ret;
		while  (iter.hasNext()) {
			entry = iter.next();
			key = entry.getKey();
			latest = latest.next = new RequestPipeRequest(key.item, key.nbt == null || key.nbt.isEmpty() ? 0 : key.nbt.hashCode(), entry.getValue().reqCount);
		}
		return ret;
	}

	public static class Key {
		public final Item item;
		public final NbtCompound nbt;

		public Key(Item item, NbtCompound nbt) {
			this.item = item;
			this.nbt = nbt;
		}
		public Key(NbtCompound tag) {
			this.item = Registry.ITEM.get(new Identifier(tag.getString("item")));
			NbtCompound nbt = tag.getCompound("nbt");
			this.nbt = nbt.isEmpty() ? null : nbt;
		}
		public void writeNbt(NbtCompound tag) {
			tag.putString("item", Registry.ITEM.getId(item).toString());
			if (nbt != null) tag.put("nbt", nbt);
		}
		public NbtCompound toNbt(NbtCompound tag) {
			writeNbt(tag);
			return tag;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				if (item.equals(((Key) obj).item)) {
					if (nbt == null) return ((Key) obj).nbt == null;
					return nbt.equals(((Key) obj).nbt);
				}
			}
			return false;
		}
		@Override
		public int hashCode() {
			if (nbt == null) return item.hashCode();
			return item.hashCode() ^ nbt.hashCode();
		}
	}
	public static class Data {
		public int count;
		public int reqCount = 0;
		public final Set<RequestPipeCache.InvNode> cachePos;
		public Data(int count, Set<RequestPipeCache.InvNode> cachePos) {
			this.count = count;
			this.cachePos = cachePos;
		}
		public Data(NbtCompound tag) {
			cachePos = null;
			reqCount = tag.getInt("reqCount");
		}
		public void writeNbt(NbtCompound tag) {
			if (reqCount>0)
				tag.putInt("reqCount", reqCount);
		}
		public NbtCompound toNbt(NbtCompound tag) {
			writeNbt(tag);
			return tag;
		}
		public static Data fromNbt(NbtCompound tag) {
			if (tag == null || !tag.contains("reqCount")) return null;
			return new Data(tag);
		}
	}
}
