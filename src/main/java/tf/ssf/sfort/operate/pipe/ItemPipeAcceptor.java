package tf.ssf.sfort.operate.pipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public interface ItemPipeAcceptor {
    boolean acceptItemFrom(ItemStack stack, Direction dir);
}
