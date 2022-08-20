package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import tf.ssf.sfort.operate.Main;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class FilterPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<FilterPipeEntity> ENTITY_TYPE;
	public Item[] filterOutSides = new Item[]{Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR};
	public Item[] filterInSides = new Item[]{Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR};

	public FilterPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("filter_pipe"), FabricBlockEntityTypeBuilder.create(FilterPipeEntity::new, FilterPipe.BLOCK).build(null));
	}
	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		NbtCompound nbt = new NbtCompound();
		for (int i = 0; i< filterOutSides.length; i++) {
			nbt.putString(Integer.toString(i), Registry.ITEM.getId(filterOutSides[i]).toString());
		}
		tag.put("filter$out", nbt);
		nbt = new NbtCompound();
		for (int i = 0; i< filterInSides.length; i++) {
			nbt.putString(Integer.toString(i), Registry.ITEM.getId(filterInSides[i]).toString());
		}
		tag.put("filter$in", nbt);
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		NbtCompound nbt = new NbtCompound();
		for (int i = 0; i< filterOutSides.length; i++) {
			nbt.putString(Integer.toString(i), Registry.ITEM.getId(filterOutSides[i]).toString());
		}
		tag.put("filter$out", nbt);
		nbt = new NbtCompound();
		for (int i = 0; i< filterInSides.length; i++) {
			nbt.putString(Integer.toString(i), Registry.ITEM.getId(filterInSides[i]).toString());
		}
		tag.put("filter$in", nbt);
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		NbtCompound nbt = tag.getCompound("filter$out");
		for (int i = 0; i<filterOutSides.length; i++) {
			filterOutSides[i] = Registry.ITEM.get(Identifier.tryParse(nbt.getString(Integer.toString(i))));
		}
		nbt = tag.getCompound("filter$in");
		for (int i = 0; i<filterInSides.length; i++) {
			filterInSides[i] = Registry.ITEM.get(Identifier.tryParse(nbt.getString(Integer.toString(i))));
		}
		markDirty();
	}
	@Override
	public List<Direction> getOutputs(TransportedStack transport){
		List<Direction> ret = Arrays.stream(Direction.values()).filter(d ->
				transport.origin != d
				&& (connectedSides & (1 << d.ordinal())) != 0
				&& (filterOutSides[d.ordinal()] == Items.AIR || transport.stack.isOf(filterOutSides[d.ordinal()]))
		).collect(Collectors.toList());
		Collections.shuffle(ret);
		ret.sort((d1, d2) -> (filterOutSides[d1.ordinal()] == filterOutSides[d2.ordinal()]) ? 0 : filterOutSides[d1.ordinal()] == Items.AIR ? 1 : -1);
		return ret;
	}
	@Override
	public boolean acceptItemFrom(TransportedStack stack, Direction dir) {
		Item item = filterInSides[dir.ordinal()];
		if (item != Items.AIR && !stack.stack.isOf(item)) return false;
		return super.acceptItemFrom(stack, dir);
	}
	@Override
	public Block asBlock() {
		return FilterPipe.BLOCK;
	}
}
