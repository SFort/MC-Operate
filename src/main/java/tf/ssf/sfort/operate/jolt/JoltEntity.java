package tf.ssf.sfort.operate.jolt;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import tf.ssf.sfort.operate.Main;

import java.util.Collection;

public class JoltEntity extends BlockEntity implements Inventory {
    public static BlockEntityType<JoltEntity> ENTITY_TYPE;

    public static void register() {
        ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("jolt"), FabricBlockEntityTypeBuilder.create(JoltEntity::new, Jolt.BLOCK).build(null));
    }

    public ItemStack inv = ItemStack.EMPTY;
    private byte dir = 0;

    public void update_dir(Direction d) {
        if (world != null && d != null) {
            byte b_dir = (byte) (1 << d.getId());
            BlockState state = world.getBlockState(pos.offset(d));
            BlockState placing = Block.getBlockFromItem(inv.getItem()).getDefaultState();
            boolean bl = state.getBlock() instanceof PistonHeadBlock && state.get(Properties.FACING).equals(d.getOpposite());
            BlockPos place_pos = bl ? pos.offset(d.getOpposite()) : pos.offset(d);
            Runnable place = () -> {
                if (inv != ItemStack.EMPTY && inv.getItem() instanceof BlockItem && world.getBlockState(place_pos).isAir()) {
                    inv.decrement(1);
                    Collection<Property<?>> p = placing.getBlock().getDefaultState().getProperties();
                    if (p.contains(Properties.FACING))
                        world.setBlockState(place_pos, placing.with(Properties.FACING, d));
                    else if (p.contains(Properties.AXIS))
                        world.setBlockState(place_pos, placing.with(Properties.AXIS, d.getAxis()));
                    else
                        world.setBlockState(place_pos, placing);
                }
            };
            if (bl) {
                //didn't like the push feature
                //if re enabled in future fix constant place bug
                //place.run();
                dir |= b_dir;
            } else if ((b_dir & dir) != 0) {
                dir ^= b_dir;
                place.run();
            }
        } else {
            dir = 0;
        }
        markDirty();
    }

    public JoltEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState state) {
        super(blockEntityType, blockPos, state);
    }

    public JoltEntity(BlockPos blockPos, BlockState state) {
        super(ENTITY_TYPE, blockPos, state);
    }

    public void dropInv() {
        if (world != null && inv != ItemStack.EMPTY)
            world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, inv.copy()));
    }

    public void replaceStack(ItemStack item) {
        dropInv();
        inv = item;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) inv = stack;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        inv = ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return inv == ItemStack.EMPTY;
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot == 0 ? inv : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot == 0) {
            ItemStack i = inv;
            clear();
            return i;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot == 0 && inv != ItemStack.EMPTY) {
            return inv.split(amount);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        NbtCompound t = new NbtCompound();
        inv.writeNbt(t);
        tag.put("item", t);
        tag.putByte("dir", dir);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        inv = ItemStack.fromNbt(tag.getCompound("item"));
        dir = tag.getByte("dir");
        markDirty();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markDirty() {
        if (world != null) {
            world.markDirty(pos);
            if (!world.isClient()) {
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        tag.put("item", inv.writeNbt(new NbtCompound()));
        return tag;
    }

}
