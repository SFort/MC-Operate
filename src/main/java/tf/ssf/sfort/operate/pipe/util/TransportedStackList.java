package tf.ssf.sfort.operate.pipe.util;

import net.minecraft.nbt.NbtCompound;
import tf.ssf.sfort.operate.util.SyncableLinkedList;

import java.util.function.Supplier;


public class TransportedStackList extends SyncableLinkedList<TransportedStack> {
	@Override
	public Supplier<NbtCompound> popSync() {
		if (!isSyncable()) return null;
		TransportedStack ret = oldestRequiredSync.item;

		do {
			oldestRequiredSync = oldestRequiredSync.next;
			if (oldestRequiredSync == null) return ret::toClientTag;
			if (oldestRequiredSync.item.travelTime != ret.travelTime) return ret::toClientTag;
			if (oldestRequiredSync.item.origin != ret.origin) return ret::toClientTag;
		} while (oldestRequiredSync.item.stack.isOf(ret.stack.getItem()));
		do {
			oldestRequiredSync = oldestRequiredSync.next;
		} while (oldestRequiredSync != null && oldestRequiredSync.item.travelTime == ret.travelTime && oldestRequiredSync.item.origin == ret.origin);
		return () -> {
			NbtCompound nbt = ret.toClientTag();
			NbtCompound t = new NbtCompound();
			t.putBoolean("skipped", true);
			nbt.put("client", t);
			return nbt;
		};
	}

}
