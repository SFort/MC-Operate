package tf.ssf.sfort.operate.pipe.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import tf.ssf.sfort.operate.pipe.util.TransportedStack;

public interface ItemPipeAcceptor {
    boolean acceptItemFrom(TransportedStack stack, Direction dir);
    default boolean acceptItemFrom(ItemStack stack, Direction dir) {
        return acceptItemFrom(new TransportedStack(stack, dir, 0), dir);
    }
}
