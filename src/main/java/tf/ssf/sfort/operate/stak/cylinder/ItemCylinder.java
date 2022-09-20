package tf.ssf.sfort.operate.stak.cylinder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Config;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.Sounds;
import tf.ssf.sfort.operate.Spoon;
import tf.ssf.sfort.operate.stak.BitStak;
import tf.ssf.sfort.operate.stak.BitStakEntity;

import java.util.List;

public class ItemCylinder extends Item {

	public static Item ITEM;

	public static void register() {
		if (Config.cylinder == null) return;
		ITEM = Registry.register(Registry.ITEM, Main.id("bit_cylinder"), new ItemCylinder());
		if (Config.cylinder) {
			Spoon.INFUSE.put(new Pair<>(Items.PAPER, BitStak.BLOCK), (world, pos, state, offhand, context) -> {
				if (world.isClient || !state.isOf(BitStak.BLOCK) || state.get(BitStak.POWERED)) return null;
				BlockEntity e = world.getBlockEntity(pos);
				if (!(e instanceof BitStakEntity) || ((BitStakEntity) e).instructions.isEmpty()) return null;
				ItemStack bp = ItemCylinder.ITEM.getDefaultStack();
				NbtCompound tag = bp.getOrCreateNbt();
				tag.put("insns", ((BitStakEntity) e).getInsnsTag());
				((BitStakEntity) e).instructions.clear();
				PlayerEntity player = context.getPlayer();
				if (offhand.getCount() > 1) {
					offhand.decrement(1);
					player.dropItem(offhand, true);
				}
				player.setStackInHand(Hand.OFF_HAND, bp);
				world.playSound(null, pos, Sounds.BIT_CYLINDER, SoundCategory.PLAYERS, 1, .8f + .2f * world.random.nextFloat());
				return ActionResult.SUCCESS;
			});
		}
	}

	public ItemCylinder() {
		super(new Settings().group(ItemGroup.TOOLS).maxCount(8));
	}

	public ItemCylinder(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		ItemStack stack = context.getStack();
		if (!stack.isOf(ITEM)) return ActionResult.PASS;
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		NbtCompound nbt = stack.getNbt();
		if (nbt != null && !nbt.isEmpty()) {
			if (state.isOf(BitStak.BLOCK) && !state.get(BitStak.POWERED)) {
				if (!world.isClient()) {
					BlockEntity entity = world.getBlockEntity(pos);
					if (entity instanceof BitStakEntity) {
						NbtCompound tag = nbt.getCompound("insns");
						int i = 0;
						List<Item> insns = ((BitStakEntity) entity).instructions;
						while (true) {
							NbtElement element = tag.get(Integer.toString(i));
							if (element instanceof NbtString) {
								Item item = Registry.ITEM.get(new Identifier(element.asString()));
								if (item != Items.AIR) {
									if (insns.size() < BitStakEntity.MAX_INSN || BitStak.VALID_INSNS.containsKey(item)) {
										insns.add(item);
									} else {
										PlayerEntity player = context.getPlayer();
										if (player != null) player.dropItem(item.getDefaultStack(), true);
									}
								}
							} else break;
							i++;
						}
						stack.decrement(1);
						world.playSound(null, pos, Sounds.BIT_CYLINDER, SoundCategory.PLAYERS, 1, .8f + .2f * world.random.nextFloat());
					}
				}
				return ActionResult.SUCCESS;
			} else if (state.isOf(Cylinder.BLOCK) && state.get(Cylinder.STAGE) == 0) {
				if (!world.isClient()) {
					BlockEntity entity = world.getBlockEntity(pos);
					if (entity instanceof CylinderEntity) {
						((CylinderEntity) entity).setCylinderSchematic(stack);
					}
				}
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.PASS;
	}
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!stack.hasNbt()) stack.decrement(1);
	}
	@Override
	public boolean canBeNested() {
		return false;
	}


}