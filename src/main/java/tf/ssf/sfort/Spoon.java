package tf.ssf.sfort;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class Spoon extends Item {
    public static final SoundEvent HIT = new SoundEvent(Main.id("hit_spoon"));
    public static SoundEvent BREAK = new SoundEvent(Main.id("break_spoon"));
    public static void register(){
        //Registry.register(Registry.SOUND_EVENT, HIT.getId(), HIT);
        //Registry.register(Registry.SOUND_EVENT, BREAK.getId(), BREAK);
        Registry.register(Registry.ITEM, Main.id("wood_spoon"),new Spoon());
    }
    public Spoon(){
        super(new Settings().group(ItemGroup.TOOLS).maxDamage(4));
    }
    public Spoon(Settings settings) {
        super(settings);
    }
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();
        PlayerEntity p = context.getPlayer();
        stack.damage(-1, world.random, null);
        if (stack.getDamage() == 0){
            if(hasCrafted(world,pos))
                world.playSound(p, pos, BREAK, SoundCategory.BLOCKS, 0.27F, RANDOM.nextFloat() * 0.1F + 0.9F);
            else
                world.playSound(p, pos, HIT, SoundCategory.BLOCKS, 0.27F, RANDOM.nextFloat() * 0.1F + 0.4F);
            stack.damage(stack.getMaxDamage(), world.random, null);
        }else{
            world.playSound(p, pos, HIT, SoundCategory.BLOCKS, 0.5F, RANDOM.nextFloat() * 0.1F + 0.8F+(stack.getDamage()*0.05F));
        }
        return ActionResult.SUCCESS;
    }
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(!selected && stack.getDamage() != this.getMaxDamage()) stack.setDamage(this.getMaxDamage());
    }
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        stack.setDamage(this.getMaxDamage());
    }

    public boolean hasCrafted(World world, BlockPos pos){
        BlockState state = world.getBlockState(pos.down());
        boolean bl = world.getBlockState(pos).isOf(Blocks.OBSIDIAN) && state.isOf(Blocks.DISPENSER);
        if (bl) {
            world.removeBlock(pos, false);
            if(world instanceof ServerWorld)((ServerWorld)world).spawnParticles(ParticleTypes.ASH, pos.getX()+0.5, pos.getY()+0.6, pos.getZ()+0.5, 12, 0.3, 0.15, 0.3, 0.01);
            world.setBlockState(pos.down(), ObsidianDispenser.BLOCK.getDefaultState().with(DispenserBlock.FACING, state.get(DispenserBlock.FACING)));
        }
        return bl;
    }
}
