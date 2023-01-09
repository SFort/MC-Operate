package tf.ssf.sfort.operate;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class Sounds {
	public static final SoundEvent SPOON_HIT = SoundEvent.of(Main.id("hit_spoon"));
	public static final SoundEvent SPOON_BREAK = SoundEvent.of(Main.id("break_spoon"));
	public static final SoundEvent LOADER_LOCK = SoundEvent.of(Main.id("loader_lock"));
	public static final SoundEvent BIT_CYLINDER = SoundEvent.of(Main.id("bit_cylinder"));
	//TODO pipe sounds
	public static BlockSoundGroup PIPE_BLOCK_SOUNDS = new BlockSoundGroup(1.0F, 1.0F, SoundEvents.BLOCK_METAL_BREAK, SoundEvents.BLOCK_STONE_STEP, SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE, SoundEvents.BLOCK_METAL_HIT, SoundEvents.BLOCK_METAL_FALL);
	public static void register() {
		Registry.register(Registries.SOUND_EVENT, Sounds.SPOON_HIT.getId(), Sounds.SPOON_HIT);
		Registry.register(Registries.SOUND_EVENT, Sounds.SPOON_BREAK.getId(), Sounds.SPOON_BREAK);
		Registry.register(Registries.SOUND_EVENT, Sounds.LOADER_LOCK.getId(), Sounds.LOADER_LOCK);
		Registry.register(Registries.SOUND_EVENT, Sounds.BIT_CYLINDER.getId(), Sounds.BIT_CYLINDER);
	}
}
