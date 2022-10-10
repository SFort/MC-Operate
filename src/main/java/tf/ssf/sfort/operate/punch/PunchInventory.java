package tf.ssf.sfort.operate.punch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeMatcher;

import java.util.Arrays;

public class PunchInventory extends CraftingInventory {
    public ItemStack[] inv = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    public static final int[] sequence = new int[]{0, 3, 1, 4, 6, 7, 2, 5, 8};

    public PunchInventory() {
        super(null, 0, 0);
    }

    public ItemStack popStack() {
        for (int i : sequence)
            if (!inv[i].isEmpty()) {
                ItemStack ret = inv[i];
                inv[i] = ItemStack.EMPTY;
                return ret;
            }
        return ItemStack.EMPTY;
    }

    public ItemStack pushStack(ItemStack item) {
        for (int i : sequence)
            if (inv[i].isEmpty()) {
                inv[i] = item.copy();
                return ItemStack.EMPTY;
            }
        ItemStack ret = inv[0];
        inv[0] = item;
        return ret;
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        for (int i = 0; i < size(); i++)
            if (!inv[i].isEmpty())
                tag.put(Integer.toString(i), inv[i].writeNbt(new NbtCompound()));
        return tag;
    }

    public void readNbt(NbtCompound tag) {
        for (int i = 0; i < size(); i++) {
            final String si = Integer.toString(i);
            if (tag.contains(si)) {
                inv[i] = ItemStack.fromNbt(tag.getCompound(si));
            } else if (!inv[i].isEmpty()) {
                inv[i] = ItemStack.EMPTY;
            }
        }
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack item : inv)
            finder.addUnenchantedInput(item);
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : inv)
            if (!itemStack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack ret = inv[slot];
        inv[slot] = ItemStack.EMPTY;
        return ret;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return inv[slot].isEmpty();
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inv[slot] = stack;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inv[slot];
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        Arrays.fill(inv, ItemStack.EMPTY);
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int size() {
        return 9;
    }
}
