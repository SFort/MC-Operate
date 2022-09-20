package tf.ssf.sfort.operate.util;

public class SyncableLinkedList<T> {
	public static final Node LOCK = new Node<>(null);
	public Node<T> first;
	public Node<T> last;
	public Node<T> oldestRequiredSync;

	public void clear() {
		oldestRequiredSync = null;
		while(first != null) {
			last = first.next;
			first.clear();
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
		Node<T> next = first.next;
		first.clear();
		first = next;
		if (first == null) last = null;
	}

	public boolean isEmpty() {
		return first == null;
	}

	public T popSync() {
		if (!isSyncable()) return null;
		T ret = oldestRequiredSync.item;
		oldestRequiredSync = oldestRequiredSync.next;
		return ret;
	}

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
		public void clear() {
			item = null;
			next = null;
		}
	}
}
