package tf.ssf.sfort.operate;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Spoonable {
    ActionResult operate$onUse(BlockState blockState, World world, BlockPos blockPos, ItemUsageContext blockHitResult);
}
