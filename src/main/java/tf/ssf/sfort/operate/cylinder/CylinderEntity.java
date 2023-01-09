package tf.ssf.sfort.operate.cylinder;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.stak.BitStakEntity;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class CylinderEntity extends BlockEntity implements Inventory {
	public static BlockEntityType<CylinderEntity> ENTITY_TYPE;
	public static Comparator<Item> missingItemsComparator = Comparator.comparing(Registries.ITEM::getId);
	public static void register() {
		ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("cylinder"), FabricBlockEntityTypeBuilder.create(CylinderEntity::new, Cylinder.BLOCK).build(null));
	}

	public NbtCompound cylinderInsns = new NbtCompound();
	public Text cylinderName = null;
	public TreeMap<Item, Integer> missingItems = new TreeMap<>(missingItemsComparator);
	public Item nextMissingItem = Items.AIR;

	public CylinderEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState state) {
		super(blockEntityType, blockPos, state);
	}

	public CylinderEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public void setCylinderSchematic(ItemStack stack){
		clear();
		if (stack.isOf(ItemCylinder.ITEM)){
			NbtCompound tag = stack.getOrCreateSubNbt("insns");
			if (tag != null && !tag.isEmpty()) {
				cylinderInsns = tag.copy();
				cylinderCreated();
			}
			if (stack.hasCustomName()) cylinderName = stack.getName();
			else cylinderName = null;
		}
		setStage();
	}
	public ItemStack createCylinder() {
		if (getStage() == 2) {
			cylinderCreated();
			cylinderCreated();
			return getCylinder();
		}
		return ItemStack.EMPTY;
	}
	public ItemStack getCylinder(){
		ItemStack ret = ItemCylinder.ITEM.getDefaultStack();
		ret.getOrCreateNbt().put("insns", cylinderInsns.copy());
		if (cylinderName != null) ret.setCustomName(cylinderName);
		return ret;
	}
	public void cylinderCreated(){
		BitStakEntity.parseInsnsTag(cylinderInsns, itm -> missingItems.merge(itm, 1, Integer::sum));
		world.playSound(null, pos, Sounds.BIT_CYLINDER, SoundCategory.PLAYERS, 1, .8f + .2f * world.random.nextFloat());
		updateNext();
		markDirty();
	}
	public int getStage(){
		if (cylinderInsns.isEmpty()) return 0;
		if (missingItems.isEmpty()) return 2;
		return 1;
	}
	@Override
	public void clear(){
		if (!cylinderInsns.isEmpty())
			cylinderInsns = new NbtCompound();
		missingItems.clear();
		cylinderName = null;
		updateNext();
		markDirty();
	}
	public void setStage(){
		world.setBlockState(pos, world.getBlockState(pos).with(Cylinder.STAGE, getStage()));
	}
	public void addMissingItems(ItemStack stack) {
		Map.Entry<Item, Integer> entry = missingItems.firstEntry();
		if (!stack.isOf(entry.getKey())) return;
		int diff = entry.getValue() - stack.getCount();
		if (diff <= 0) missingItems.remove(entry.getKey());
		else missingItems.put(entry.getKey(), diff);
		if (missingItems.isEmpty()) setStage();
		updateNext();
		markDirty();
	}
	public void updateNext(){
		Map.Entry<Item, Integer> entry = missingItems.firstEntry();
		Item item = entry == null ? Items.AIR : entry.getKey();
		if (item != nextMissingItem) {
			nextMissingItem = item;
			if (world != null && !world.isClient()) {
				((ServerWorld) world).getChunkManager().markForUpdate(getPos());
			}
		}
	}
	public void dropInv() {
		if (world != null && !cylinderInsns.isEmpty()) {
			TreeMap<Item, Integer> originalMap = new TreeMap<>(missingItemsComparator);
			BitStakEntity.parseInsnsTag(cylinderInsns, itm -> originalMap.merge(itm, 1, Integer::sum));
			Consumer<ItemStack> splitAndDrop = LootTable.processStacks(
					(new LootContext.Builder((ServerWorld)this.world)).random(this.world.random).parameter(LootContextParameters.BLOCK_ENTITY, this).build(LootContextTypes.EMPTY),
					stack -> world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack)));
			for (Map.Entry<Item, Integer> entry : originalMap.entrySet()) {
				Item item = entry.getKey();
				Integer i2 = missingItems.get(item);
				Integer i1 = entry.getValue();
				if (i1 == null || i1 <= 0) continue;
				int i = i2 == null || i2 <= 0 ? i1 : (i1-i2);
				if (i<=0) continue;
				ItemStack stack = item.getDefaultStack();
				stack.setCount(i);
				splitAndDrop.accept(stack);
			}
			missingItems = originalMap;
			updateNext();
			markDirty();
		}
	}
	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.put("cylinderInsns", cylinderInsns.copy());
		NbtCompound t = new NbtCompound();
		int i = 0;
		for (Map.Entry<Item, Integer> entry : missingItems.entrySet()) {
			String key = Integer.toString(i++);
			t.putString(key, Registries.ITEM.getId(entry.getKey()).toString());
			t.putInt(key+"i", entry.getValue());
		}
		tag.put("missingItems", t);
		if (cylinderName != null) {
			tag.putString("cylinderName", Text.Serializer.toJson(cylinderName));
		}
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		if (world != null && world.isClient()) {
			nextMissingItem = Registries.ITEM.get(new Identifier(tag.getString("nmi")));
			return;
		}
		cylinderInsns = tag.getCompound("cylinderInsns");
		missingItems = new TreeMap<>(missingItemsComparator);
		int i = 0;
		NbtCompound mTag = tag.getCompound("missingItems");
		if (mTag != null && !mTag.isEmpty()) {
			while (true) {
				String key = Integer.toString(i++);
				try {
					Item item = Registries.ITEM.get(new Identifier(mTag.getString(key)));
					int count = mTag.getInt(key + "i");
					if (count > 0 && item != Items.AIR) {
						missingItems.put(item, count);
					} else break;
				} catch (RuntimeException ignore) {
					break;
				}
			}
		}
		try {
			cylinderName = Text.Serializer.fromJson(tag.getString("cylinderName"));
		} catch (Exception exception) {
			cylinderName = null;
		}

		updateNext();
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = new NbtCompound();
		tag.putString("nmi", Registries.ITEM.getId(nextMissingItem).toString());
		return tag;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		if (isEmpty()){
			addMissingItems(stack);
			return;
		}
		cylinderCreated();
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return false;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return getStage() != 2;
	}

	@Override
	public int getMaxCountPerStack() {
		if (isEmpty()){
			Integer count = missingItems.firstEntry().getValue();
			if (count == null) return 0;
			return Math.min(64, count);
		}
		return 1;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if (isEmpty()) {
			Map.Entry<Item, Integer> entry = missingItems.firstEntry();
			if (entry == null) return false;
			return stack.isOf(entry.getKey());
		}
		return false;
	}


	@Override
	public ItemStack getStack(int slot) {
		if (isEmpty()) return ItemStack.EMPTY;
		return getCylinder();
	}

	@Override
	public void markDirty() {
		if (world != null) {
			world.markDirty(pos);
		}
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack ret = getStack(slot);
		setStack(slot, ItemStack.EMPTY);
		return ret;
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		return removeStack(slot);
	}

}
