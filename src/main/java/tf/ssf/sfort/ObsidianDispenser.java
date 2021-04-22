package tf.ssf.sfort;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObsidianDispenser extends DispenserBlock {
	public static Block BLOCK;
	private static final Map<Item, DispenserBehavior> BEHAVIORS = new HashMap<>();
	public ObsidianDispenser() {
		super(Settings.of(Material.STONE, MaterialColor.BLACK).requiresTool().strength(50.0F, Blocks.OBSIDIAN.getBlastResistance()));
	}
	public ObsidianDispenser(Settings settings) {
		super(settings);
	}
	public static void registerBehavior(ItemConvertible provider, DispenserBehavior behavior) {
		BEHAVIORS.put(provider.asItem(), behavior);
	}
	protected DispenserBehavior getBehaviorForItem(ItemStack stack) {
		if (BEHAVIORS.containsKey(stack.getItem()))
			return BEHAVIORS.get(stack.getItem());
		return super.getBehaviorForItem(stack);
	}
	public static void register() {
		if (Config.obsDispenser != null)
			BLOCK = Registry.register(Registry.BLOCK, Main.id("obs_dispenser"), new ObsidianDispenser());
	}
	public static void registerPowder(){
		if (Config.dispenseGunpowder != null) {
			if (Config.dispenseGunpowder)
				DispenserBlock.registerBehavior(Items.GUNPOWDER, new ItemDispenserBehavior() {
					public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
						return ignite(pointer, stack, 1);
					}
				});
			registerBehavior(Items.GUNPOWDER, new ItemDispenserBehavior() {
				public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
					return ignite(pointer, stack, 4);
				}
			});
		}
	}
	public static void registerPearl(){
		/*if (Config.dispenseEnderpearls)
			DispenserBlock.registerBehavior(Items.GUNPOWDER, new ItemDispenserBehavior() {
					public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
						return ignite(pointer, stack, 1);
					}
				});*/
	}
	public static ItemStack ignite(BlockPointer pointer, ItemStack stack, int min){
		BlockPos pos = pointer.getBlockPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
		if (stack.getCount() < min) min = stack.getCount();
		stack.decrement(min);
		ServerWorld w = pointer.getWorld();
		if (w.isAir(pos)){
			Box box = new Box(pos);
			w.spawnParticles(ParticleTypes.FLAME, pos.getX()+0.5, pos.getY()+0.6, pos.getZ()+0.5, min*2, 0.3, 0.15, 0.3, 0.01);
			List<LivingEntity> list = w.getEntitiesByClass(LivingEntity.class, box,null);
				for (LivingEntity e : list) {
					e.damage(DamageSource.explosion((Explosion) null), (float) min);
				}
		}else
			pointer.getWorld().createExplosion(null, pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5, min, Explosion.DestructionType.BREAK);
		return stack;
	}
	@Override
	public Item asItem(){ return Items.DISPENSER; };
}
