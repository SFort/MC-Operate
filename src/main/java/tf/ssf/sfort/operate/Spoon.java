package tf.ssf.sfort.operate;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Spoon extends Item {
	public static final SoundEvent HIT = new SoundEvent(Main.id("hit_spoon"));
	public static SoundEvent BREAK = new SoundEvent(Main.id("break_spoon"));
	public static Map<Pair<Block,Block>, SpoonDo> CRAFT = new HashMap<>();
	public static Map<Item, Predicate<ItemUsageContext>> PLACE = new HashMap<>();
	public static Item ITEM;

	public static void register() {
		//Registry.register(Registry.SOUND_EVENT, HIT.getId(), HIT);
		//Registry.register(Registry.SOUND_EVENT, BREAK.getId(), BREAK);
		ITEM = Registry.register(Registry.ITEM, Main.id("wood_spoon"), new Spoon());
	}

	public Spoon() {
		super(new Settings().group(ItemGroup.TOOLS).maxDamage(4));
	}

	public Spoon(Settings settings) {
		super(settings);
	}

	public ActionResult useOnBlock(ItemUsageContext context) {
		{
			PlayerEntity p = context.getPlayer();
			if (p != null) {
				Predicate<ItemUsageContext> placeBlock = PLACE.get(p.getOffHandStack().getItem());
				if (placeBlock != null) {
					if (placeBlock.test(context)) return ActionResult.SUCCESS;
				}
			}
		}
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		ItemStack stack = context.getStack();
		stack.setDamage(stack.getDamage()-1);

		if (stack.getDamage() == 0) {
			BlockState state = world.getBlockState(pos);
			BlockPos cpos = pos.offset(context.getSide().getOpposite());
			BlockState cstate = world.getBlockState(cpos);
			Pair<Block, Block> key = new Pair<>(state.getBlock(), cstate.getBlock());
			if (CRAFT.containsKey(key)){
				CRAFT.get(key).act(world, pos, cpos, state, cstate);
			} else if (world instanceof ServerWorld) {
				world.playSound(null, pos, HIT, SoundCategory.BLOCKS, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.8F + (stack.getDamage() * 0.05F));
			}
			if (Config.litSpoon && state.getProperties().contains(Properties.LIT)) {
				if (world instanceof ServerWorld) {
					world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(pos, state.with(Properties.LIT, true), 0);
			}
			stack.setDamage(getMaxDamage());
		} else if (world instanceof ServerWorld) {
			world.playSound(null, pos, HIT, SoundCategory.BLOCKS, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.8F + (stack.getDamage() * 0.05F));
		}
		return ActionResult.SUCCESS;
	}

	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!selected && stack.getDamage() != this.getMaxDamage()) stack.setDamage(this.getMaxDamage());
	}

	public void onCraft(ItemStack stack, World world, PlayerEntity player) {
		stack.setDamage(this.getMaxDamage());
	}

	//Still bad but better
	public interface SpoonDo{
		void act(World world, BlockPos pos, BlockPos cpos, BlockState state, BlockState cstate);
	}

}