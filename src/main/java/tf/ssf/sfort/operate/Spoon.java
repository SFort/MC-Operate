package tf.ssf.sfort.operate;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class Spoon extends Item {
	public static Map<Pair<Block,Block>, SpoonDo> CRAFT = new HashMap<>();
	public static Map<Item, SpoonDoLessSided> PLACE = new HashMap<>();
	public static Map<Pair<Item,Block>, SpoonDoLess> INFUSE = new HashMap<>();
	public static Item ITEM;

	public static void register() {
		ITEM = Registry.register(Registries.ITEM, Main.id("wood_spoon"), new Spoon());
	}

	public Spoon() {
		super(new Settings().maxDamage(4));
	}

	public Spoon(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		ItemStack stack = context.getStack();
		if (!stack.isOf(ITEM)) return ActionResult.PASS;
		PlayerEntity player = context.getPlayer();
		ItemStack offhandStack = player == null ? null : player.getOffHandStack();
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		if (offhandStack != null) {
			SpoonDoLessSided placeBlock = PLACE.get(offhandStack.getItem());
			if (placeBlock != null) {
				ActionResult ret = placeBlock.act(world, pos, state, offhandStack, context.getSide());
				if (ret != null) return ret;
			}
		}
		if (offhandStack != null) {
			SpoonDoLess infuse = INFUSE.get(new Pair<>(offhandStack.getItem(), state.getBlock()));
			if (infuse != null) {
				ActionResult ret = infuse.act(world, pos, state, offhandStack, context);
				if (ret != null) return ret;
			}
		}
		if (state.getBlock() instanceof Spoonable) {
			ActionResult ret = ((Spoonable)state.getBlock()).operate$onUse(state, world, pos, context);
			if (ret != null) {
				world.playSound(null, pos, Sounds.SPOON_HIT, SoundCategory.BLOCKS, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.8F + (stack.getDamage() * 0.05F));
				stack.setDamage(getMaxDamage());
				return ret;
			}
		}
		stack.setDamage(stack.getDamage()-1);
		if (stack.getDamage() == 0) {
			BlockPos cpos = pos.offset(context.getSide().getOpposite());
			BlockState cstate = world.getBlockState(cpos);
			{
				SpoonDo craft = CRAFT.get(new Pair<>(state.getBlock(), cstate.getBlock()));
				if (craft == null || !craft.act(world, pos, cpos, state, cstate)){
					world.playSound(null, pos, Sounds.SPOON_HIT, SoundCategory.BLOCKS, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.8F + (stack.getDamage() * 0.05F));
				}
			}
			if (Config.litSpoon && state.getProperties().contains(Properties.LIT)) {
				if (world instanceof ServerWorld) {
					world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F);
				}
				world.setBlockState(pos, state.with(Properties.LIT, true), 0);
			}
			stack.setDamage(getMaxDamage());
		} else if (world instanceof ServerWorld) {
			world.playSound(null, pos, Sounds.SPOON_HIT, SoundCategory.BLOCKS, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.8F + (stack.getDamage() * 0.05F));
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!selected && stack.getDamage() != this.getMaxDamage()) stack.setDamage(this.getMaxDamage());
	}

	@Override
	public void onCraft(ItemStack stack, World world, PlayerEntity player) {
		stack.setDamage(this.getMaxDamage());
	}

	@Override
	public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
		if (Registries.BLOCK.getId(state.getBlock()).getNamespace().equals("operate")) return 2f;
		return 1.0f;
	}

	//Still bad but better
	public interface SpoonDo{
		boolean act(World world, BlockPos pos, BlockPos cpos, BlockState state, BlockState cstate);
	}

	public interface SpoonDoLess{
		ActionResult act(World world, BlockPos pos, BlockState state, ItemStack offhand, ItemUsageContext context);
	}
	public interface SpoonDoLessSided{
		ActionResult act(World world, BlockPos pos, BlockState state, ItemStack offhand, Direction side);
	}
}