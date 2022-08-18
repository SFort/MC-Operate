package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.tick.OrderedTick;
import org.jetbrains.annotations.Nullable;
import tf.ssf.sfort.operate.Main;


public class EntrancePipeEntity extends AbstractPipeEntity implements SidedInventory {
    public static BlockEntityType<EntrancePipeEntity> ENTITY_TYPE;

    public EntrancePipeEntity(BlockPos blockPos, BlockState state) {
        super(ENTITY_TYPE, blockPos, state);
    }
    public static void register() {
        ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("entrance_pipe"), FabricBlockEntityTypeBuilder.create(EntrancePipeEntity::new, EntrancePipe.BLOCK).build(null));
    }

    @Override
    public boolean acceptItemFrom(TransportedStack stack, Direction dir) {
        if (world == null) return false;
        stack.travelTime = world.getLevelProperties().getTime() + getPipeTransferTime();
        stack.origin = dir;
        itemQueue.offer(stack);
        world.getBlockTickScheduler().scheduleTick(new OrderedTick<>(asBlock(), pos, stack.travelTime + 1, world.getTickOrder()));
        markDirty();
        return true;
    }

    @Override
    public int getPipeTransferTime() {
        return 10;
    }

    @Override
    public Block asBlock() {
        return EntrancePipe.BLOCK;
    }

    public void pushNewItem(ItemStack stack, Direction dir) {
    }

    @Override
    public int size() {
        return isEmpty() ? 6 : 0;
    }

    @Override
    public boolean isEmpty() {
        return itemQueue.size() < 32;
    }

    @Override
    public ItemStack getStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= 5) return;
        if ((connectedSides & (1<<slot)) == 0) return;
        pushNewItem(stack, Direction.values()[slot]);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {}

    @Override
    public int[] getAvailableSlots(Direction side) {
        int slot = side.ordinal();
        if ((connectedSides & (1<<slot)) == 0) return new int[0];
        return new int[]{slot};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return dir != null && slot == dir.ordinal() && isEmpty() && (connectedSides & (1<<slot)) != 0;
    }
    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return isEmpty();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }
}
