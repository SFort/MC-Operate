package tf.ssf.sfort.operate;

import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

public class Sounds {
	public static final SoundEvent SPOON_HIT = new SoundEvent(Main.id("hit_spoon"));
	public static final SoundEvent SPOON_BREAK = new SoundEvent(Main.id("break_spoon"));
	public static final SoundEvent LOADER_LOCK = new SoundEvent(Main.id("loader_lock"));
	public static final SoundEvent BIT_CYLINDER = new SoundEvent(Main.id("bit_cylinder"));
	//TODO pipe sounds
	public static BlockSoundGroup PIPE_BLOCK_SOUNDS = new BlockSoundGroup(1.0F, 1.0F, SoundEvents.BLOCK_METAL_BREAK, SoundEvents.BLOCK_STONE_STEP, SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE, SoundEvents.BLOCK_METAL_HIT, SoundEvents.BLOCK_METAL_FALL);
	public static void register() {
		Registry.register(Registry.SOUND_EVENT, Sounds.SPOON_HIT.getId(), Sounds.SPOON_HIT);
		Registry.register(Registry.SOUND_EVENT, Sounds.SPOON_BREAK.getId(), Sounds.SPOON_BREAK);
		Registry.register(Registry.SOUND_EVENT, Sounds.LOADER_LOCK.getId(), Sounds.LOADER_LOCK);
		Registry.register(Registry.SOUND_EVENT, Sounds.BIT_CYLINDER.getId(), Sounds.BIT_CYLINDER);
	}
}
