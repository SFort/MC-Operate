package tf.ssf.sfort.operate.punch;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import tf.ssf.sfort.operate.Main;

import java.util.Optional;

public class PunchEntity extends BlockEntity implements Inventory {
    public static BlockEntityType<PunchEntity> ENTITY_TYPE;

    public static void register() {
        ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("punch"), FabricBlockEntityTypeBuilder.create(PunchEntity::new, Punch.BLOCK).build(null));
    }

    public final PunchInventory inv = new PunchInventory();
    public Optional<CraftingRecipe> craftResult = Optional.empty();
    public ItemStack craftResultDisplay = ItemStack.EMPTY;

    public PunchEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState state) {
        super(blockEntityType, blockPos, state);
    }

    public PunchEntity(BlockPos blockPos, BlockState state) {
        super(ENTITY_TYPE, blockPos, state);
    }

    public void dropInv() {
        if (world == null) return;
        for (ItemStack item : inv.inv)
            if (!item.isEmpty())
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, item));
    }

    public int getComparator() {
        int i = 0;
        for (ItemStack item : inv.inv)
            if (!item.isEmpty()) i++;
        if (i == 9) return 15;
        return i;
    }

    public void popInv() {
        if (this.canCraft()) {
            ItemStack ret = craftResult.get().craft(inv, world.getRegistryManager());
            inv.clear();
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
            dropItem(ret);
            return;
        }
        dropItem(inv.popStack());
    }

    public void pushInv(ItemStack item) {
        dropItem(inv.pushStack(item));
    }

    public void dropItem(ItemStack item) {
        if (!item.isEmpty() && world != null)
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, item));
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.put("items", inv.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        inv.readNbt(tag.getCompound("items"));
        markDirty();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null) {
            craftResult = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inv, world).map(RecipeEntry::value);
            if (!this.world.isClient()) {
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            } else {
                if (craftResult.isPresent()) craftResultDisplay = craftResult.get().getResult(world.getRegistryManager());
                else if (!craftResultDisplay.isEmpty()) craftResultDisplay = ItemStack.EMPTY;
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        tag.put("items", inv.writeNbt(new NbtCompound()));
        return tag;
    }

    public boolean canCraft() {
        return craftResult.isPresent() && this.world != null && !this.getCachedState().get(Punch.POWERED);
    }

    @Override
    public ItemStack getStack(int slot) {
        if (this.canCraft()) {
            return craftResult.get().getResult(world.getRegistryManager());
        }
        return inv.getStack(PunchInventory.sequence[slot]);
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (this.canCraft()) {
            ItemStack ret = craftResult.get().craft(inv, world.getRegistryManager());
            inv.clear();
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
            return ret;
        }
        return inv.removeStack(PunchInventory.sequence[slot]);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (this.canCraft()) {
            craftResult = Optional.empty();
            return;
        }
        inv.setStack(PunchInventory.sequence[slot], stack);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (this.canCraft()) return false;
        return inv.isValid(PunchInventory.sequence[slot], stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return inv.canPlayerUse(player);
    }

    @Override
    public void clear() {
        inv.clear();
    }

    @Override
    public int getMaxCountPerStack() {
        return inv.getMaxCountPerStack();
    }

    @Override
    public int size() {
        return inv.size();
    }

    @Override
    public boolean isEmpty() {
        return inv.isEmpty();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

}
