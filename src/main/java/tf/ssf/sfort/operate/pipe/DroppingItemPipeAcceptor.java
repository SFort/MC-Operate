package tf.ssf.sfort.operate.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public interface DroppingItemPipeAcceptor {
    void alwaysAcceptItemFrom(TransportedStack stack, Direction dir);
    default void alwaysAcceptItemFrom(ItemStack stack, Direction dir) {
        alwaysAcceptItemFrom(new TransportedStack(stack, dir, 0), dir);
    }
}
