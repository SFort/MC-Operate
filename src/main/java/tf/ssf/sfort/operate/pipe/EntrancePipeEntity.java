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
	public Block asBlock() {
		return EntrancePipe.BLOCK;
	}

	@Override
	public int size() {
		return isEmpty() ? 6 : 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
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
		if (slot < 0 || slot > 5) return;
		acceptItemFrom(stack, Direction.values()[slot]);
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
		return new int[]{slot};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return dir != null && slot == dir.ordinal() && isEmpty();
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
