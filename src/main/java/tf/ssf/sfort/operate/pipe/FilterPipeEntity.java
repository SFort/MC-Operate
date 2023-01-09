package tf.ssf.sfort.operate.pipe;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.pipe.util.TransportedStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


public class FilterPipeEntity extends AbstractPipeEntity {
	public static BlockEntityType<FilterPipeEntity> ENTITY_TYPE;
	public Item[] filterOutSides = new Item[]{Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR};
	public Item[] filterInSides = new Item[]{Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR, Items.AIR};

	public FilterPipeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public static void register() {
		ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("filter_pipe"), FabricBlockEntityTypeBuilder.create(FilterPipeEntity::new, FilterPipe.BLOCK).build(null));
	}
	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = super.toInitialChunkDataNbt();
		NbtCompound nbt = new NbtCompound();
		for (int i = 0; i< filterOutSides.length; i++) {
			nbt.putString(Integer.toString(i), Registries.ITEM.getId(filterOutSides[i]).toString());
		}
		tag.put("filter$out", nbt);
		nbt = new NbtCompound();
		for (int i = 0; i< filterInSides.length; i++) {
			nbt.putString(Integer.toString(i), Registries.ITEM.getId(filterInSides[i]).toString());
		}
		tag.put("filter$in", nbt);
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		NbtCompound nbt = new NbtCompound();
		for (int i = 0; i< filterOutSides.length; i++) {
			nbt.putString(Integer.toString(i), Registries.ITEM.getId(filterOutSides[i]).toString());
		}
		tag.put("filter$out", nbt);
		nbt = new NbtCompound();
		for (int i = 0; i< filterInSides.length; i++) {
			nbt.putString(Integer.toString(i), Registries.ITEM.getId(filterInSides[i]).toString());
		}
		tag.put("filter$in", nbt);
	}

	@Override
	public void readNbtCommon(NbtCompound tag) {
		super.readNbtCommon(tag);
		NbtCompound nbt = tag.getCompound("filter$out");
		for (int i = 0; i<filterOutSides.length; i++) {
			filterOutSides[i] = Registries.ITEM.get(Identifier.tryParse(nbt.getString(Integer.toString(i))));
		}
		nbt = tag.getCompound("filter$in");
		for (int i = 0; i<filterInSides.length; i++) {
			filterInSides[i] = Registries.ITEM.get(Identifier.tryParse(nbt.getString(Integer.toString(i))));
		}
	}
	@Override
	public Function<TransportedStack, List<Direction>> getOutputs(){
		AtomicReference<Direction> lastDir = new AtomicReference<>();
		AtomicReference<Item> lastItem = new AtomicReference<>();
		List<Direction> ret = new ArrayList<>();
		List<Direction> out = new ArrayList<>();
		return stack -> {
			if (lastDir.get() != stack.origin || !stack.stack.isOf(lastItem.get())) {
				lastDir.set(stack.origin);
				lastItem.set(stack.stack.getItem());
				ret.clear();
				out.clear();
				for (Direction d : connectedSides) {
					if (stack.origin != d) {
						if (stack.stack.isOf(filterOutSides[d.ordinal()])) {
							out.add(d);
						} else if (filterOutSides[d.ordinal()] == Items.AIR) {
							ret.add(d);
						}
					}
				}
			}
			Collections.shuffle(ret);
			Collections.shuffle(out);
			List<Direction> r = new ArrayList<>();
			r.addAll(out);
			r.addAll(ret);
			return r;
		};
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
