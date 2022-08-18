package tf.ssf.sfort.operate.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public interface ItemPipeAcceptor {
    boolean acceptItemFrom(AbstractPipeEntity.TransportedStack stack, Direction dir);
    default boolean acceptItemFrom(ItemStack stack, Direction dir) {
        return acceptItemFrom(new AbstractPipeEntity.TransportedStack(stack, dir, 0), dir);
    }
}
