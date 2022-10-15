package tf.ssf.sfort.operate.util;

import net.minecraft.nbt.NbtCompound;

import java.util.function.Supplier;

public abstract class SyncableLinkedList<T> {
	public static final Node LOCK = new Node<>(null);
	public Node<T> first;
	public Node<T> last;
	public Node<T> oldestRequiredSync;

	public void clear() {
		oldestRequiredSync = null;
		while(first != null) {
			last = first.next;
			first = last;
		}
	}

	public void push(T t) {
		if (t == null) return;
		if (last != null) {
			last.next = new Node<>(t);
			last = last.next;
			if (oldestRequiredSync == null) oldestRequiredSync = last;
		} else oldestRequiredSync = first = last = new Node<>(t);
	}

	public T pop() {
		if (first == null) return null;
		T ret = first.item;
		progress();
		return ret;
	}
	public void progress() {
		if (first == null) return;
		if (first == oldestRequiredSync){
			oldestRequiredSync = first.next;
		}
		first = first.next;
		if (first == null) last = null;
	}

	public boolean isEmpty() {
		return first == null;
	}

	public abstract Supplier<NbtCompound> popSync();

	public void lockSync() {
		oldestRequiredSync = LOCK;
	}

	public boolean isSyncable() {
		return oldestRequiredSync != null && oldestRequiredSync != LOCK;
	}
	public static class Node<T> {
		public T item;
		public Node<T> next = null;

		Node(T element) {
			this.item = element;
		}
	}
}
