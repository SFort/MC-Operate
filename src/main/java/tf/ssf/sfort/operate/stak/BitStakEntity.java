package tf.ssf.sfort.operate.stak;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import tf.ssf.sfort.operate.Main;
import tf.ssf.sfort.operate.tube.ColorTubeEntity;
import tf.ssf.sfort.operate.tube.TubeConnectTypes;
import tf.ssf.sfort.operate.util.OperateUtil;
import tf.ssf.sfort.operate.util.StakCompute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BitStakEntity extends BlockEntity implements StakCompute {
	public static BlockEntityType<BitStakEntity> ENTITY_TYPE;
	public static int MAX_INSN = 256;

	public List<Item> instructions = new ArrayList<>();
	public int[] stack = {0, 0, 0, 0, 0, 0};
	public int stackPos = -1;
	public int insnPos = -1;
	public int redstone = 0;

	public void resetMem() {
		Arrays.fill(stack, 0);
		stackPos = -1;
		insnPos = -1;
	}

	public static void register() {
		ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("bit"), FabricBlockEntityTypeBuilder.create(BitStakEntity::new, BitStak.BLOCK).build(null));
	}

	public BitStakEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState state) {
		super(blockEntityType, blockPos, state);
	}

	public BitStakEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}

	public void serverTick(World world, BlockPos pos, BlockState state) {
		if (!state.get(BitStak.POWERED)) return;
		if (instructions.isEmpty()) return;
		insnPos++;
		world.markDirty(pos);
		if (insnPos >= instructions.size() || insnPos < 0) {
			world.setBlockState(pos, state.with(BitStak.POWERED, false));
			return;
		}
		Item itmInsn = instructions.get(insnPos);
		if (itmInsn == null || itmInsn == Items.AIR) {
			criticalErr();
			return;
		}
		Predicate<StakCompute> insn = BitStak.VALID_OPS.get(itmInsn);
		if (insn == null) {
			criticalErr();
			return;
		}
		if (!insn.test(this)) {
			criticalErr();
		}
	}

	public void criticalErr() {
		if (world != null) {
			world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 2, World.ExplosionSourceType.TNT);
		}
	}

	public void dropInv() {
		if (world == null) return;
		instructions.sort(Comparator.comparing(Registries.ITEM::getId));
		for (Item item : instructions) {
			world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, item.getDefaultStack()));
		}
		instructions.clear();
	}

	public void pushInv(ItemStack item) {
		if (instructions.size() < MAX_INSN) {
			instructions.add(item.getItem());
		} else {
			dropItem(item);
		}
	}

	public void dropItem(ItemStack item) {
		if (!item.isEmpty() && world != null)
			world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, item));
	}

	@Override
	public boolean computeAdd() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] += stack[stackPos];
		stackPos--;
		return true;
	}

	@Override
	public boolean computeSub() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] -= stack[stackPos];
		stackPos--;
		return true;
	}

	@Override
	public boolean computeGreater() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] = stack[stackPos - 1] > stack[stackPos] ? 0 : -1;
		stackPos--;
		return true;
	}

	@Override
	public boolean computeLesser() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] = stack[stackPos - 1] < stack[stackPos] ? 0 : -1;
		stackPos--;
		return true;
	}

	@Override
	public boolean computeDiv() {
		if (stackPos < 1) return false;
		if (stack[stackPos] == 0) return false;
		stack[stackPos - 1] = stack[stackPos - 1] / stack[stackPos];
		stackPos--;
		return true;
	}

	@Override
	public boolean computeMul() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] *= stack[stackPos];
		stackPos--;
		return true;
	}

	@Override
	public boolean computeAnd() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] &= stack[stackPos];
		stackPos--;
		return true;
	}

	@Override
	public boolean computeXor() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] ^= stack[stackPos];
		stackPos--;
		return true;
	}

	@Override
	public boolean computeNot() {
		if (stackPos < 0) return false;
		stack[stackPos] = stack[stackPos] == 0 ? -1 : 0;
		return true;
	}

	@Override
	public boolean computeEquals() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] = stack[stackPos] == stack[stackPos] ? 0 : 1;
		stackPos--;
		return true;
	}

	@Override
	public boolean computeDup() {
		if (stackPos < 0) return false;
		if (stackPos + 1 >= stack.length) return false;
		stack[stackPos + 1] = stack[stackPos];
		stackPos++;
		return true;
	}

	@Override
	public boolean computePop() {
		if (stackPos < 0) return false;
		stack[stackPos--] = 0;
		return true;
	}

	@Override
	public boolean computeTick() {
		if (stackPos < 0) return false;
		if (stack[stackPos] <= 0) {
			stack[stackPos--] = 0;
		} else {
			stack[stackPos]--;
			insnPos--;
		}
		return true;
	}

	@Override
	public boolean computeIf0() {
		if (stackPos < 0) return false;
		if (stack[stackPos] != 0) insnPos++;
		stack[stackPos--] = 0;
		return true;
	}

	@Override
	public boolean computeSwap() {
		if (stackPos < 1) return false;
		int tmp = stack[stackPos];
		stack[stackPos] = stack[stackPos - 1];
		stackPos--;
		stack[stackPos] = tmp;
		return true;
	}

	@Override
	public boolean computeShiftLeft() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] = stack[stackPos - 1] << stack[stackPos];
		stack[stackPos--] = 0;
		return true;
	}

	@Override
	public boolean computeShiftRight() {
		if (stackPos < 1) return false;
		stack[stackPos - 1] = stack[stackPos - 1] >>> stack[stackPos];
		stack[stackPos--] = 0;
		return true;
	}

	@Override
	public boolean computeMark() {
		if (++stackPos >= stack.length) return false;
		stack[stackPos] = insnPos;
		return true;
	}

	@Override
	public boolean computeJump() {
		if (stackPos < 0) return false;
		insnPos = stack[stackPos];
		stack[stackPos--] = 0;
		return insnPos >= -1;
	}

	@Override
	public boolean computeGetColorStrength() {
		if (world == null) return false;
		if (stackPos < 0) return false;
		int clr = stack[stackPos];
		if (clr < 1 || clr > 16) return false;
		clr--;
		BlockPos.Mutable mutable = pos.mutableCopy();
		long color = 0;
		for (Direction dir : Direction.values()) {
			BlockEntity entity = world.getBlockEntity(mutable.set(pos).move(dir));
			if (entity instanceof ColorTubeEntity) {
				color |= ((ColorTubeEntity) entity).colorLvl;
			}
		}
		TubeConnectTypes con = TubeConnectTypes.values()[clr];
		stack[stackPos] = (int) ((color & con.mask) >>> con.shift);
		return true;
	}

	@Override
	public boolean computeConst(int con) {
		if (++stackPos >= stack.length) return false;
		stack[stackPos] = con;
		return true;
	}

	@Override
	public boolean computeStore() {
		if (stackPos < 0) return false;
		int old = redstone;
		redstone = stack[stackPos] & 0xf;
		if (old != redstone) markDirty();
		stack[stackPos--] = 0;
		return true;
	}

	@Override
	public boolean computeLoad() {
		if (world == null) return false;
		if (++stackPos >= stack.length) return false;
		stack[stackPos] = world.getReceivedRedstonePower(pos);
		return true;
	}

	@Override
	public boolean computeColorLoad() {
		if (world == null) return false;
		if (++stackPos >= stack.length) return false;
		BlockPos.Mutable mutable = pos.mutableCopy();
		long color = 0;
		for (Direction dir : Direction.values()) {
			BlockEntity entity = world.getBlockEntity(mutable.set(pos).move(dir));
			if (entity instanceof ColorTubeEntity && ((ColorTubeEntity) entity).sides[dir.getOpposite().ordinal()] == TubeConnectTypes.ALL) {
				color |= ((ColorTubeEntity) entity).colorLvl;
			}
		}
		stack[stackPos] = TubeConnectTypes.compressColor(color);
		return true;
	}

	@Override
	public boolean computeColorAdd() {
		if (world == null) return false;
		if (stackPos < 0) return false;
		int clr = stack[stackPos] & 0xffff;
		applyToAllConnectedColorTubes(e -> e.addCompressedColor(clr));
		stack[stackPos--] = 0;
		return true;
	}

	@Override
	public boolean computeColorSubtract() {
		if (world == null) return false;
		if (stackPos < 0) return false;
		int clr = stack[stackPos] & 0xffff;
		applyToAllConnectedColorTubes(e -> e.subCompressedColor(clr));
		stack[stackPos--] = 0;
		return true;
	}

	public void applyToAllConnectedColorTubes(Consumer<ColorTubeEntity> process) {
		for (ColorTubeEntity cte : OperateUtil.getConnecting(world, pos, ColorTubeEntity.class, ColorTubeEntity::hasAllColorConnection)){
			process.accept(cte);
		}
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putInt("stackPos", stackPos);
		tag.putInt("insnPos", insnPos);
		tag.putInt("redstone", redstone);
		NbtCompound stak = new NbtCompound();
		for (int i = 0; i < stack.length; i++) {
			stak.putInt(Integer.toString(i), stack[i]);
		}
		tag.put("stack", stak);
		tag.put("insns", getInsnsTag());
	}
	public void writeInsnsTag(NbtCompound tag) {
		for (int i = 0; i < instructions.size(); i++) {
			tag.putString(Integer.toString(i), Registries.ITEM.getId(instructions.get(i)).toString());
		}
	}
	public NbtCompound getInsnsTag() {
		NbtCompound tag = new NbtCompound();
		writeInsnsTag(tag);
		return tag;
	}

	public static void parseInsnsTag(NbtCompound tag, Consumer<Item> adder) {
		int i = 0;
		while (true) {
			NbtElement nbt = tag.get(Integer.toString(i));
			if (nbt instanceof NbtString) {
				Item item = Registries.ITEM.get(new Identifier(nbt.asString()));
				if (item != Items.AIR) {
					adder.accept(item);
				}
			} else break;
			i++;
		}
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		stackPos = Math.max(0, Math.min(15, tag.getInt("stackPos")));
		redstone = Math.max(0, Math.min(15, tag.getInt("redstone")));
		NbtCompound stak = tag.getCompound("stack");
		for (int i = 0; i < stack.length; i++) {
			NbtElement nbt = stak.get(Integer.toString(i));
			if (nbt instanceof NbtInt) {
				stack[i] = ((NbtInt) nbt).intValue();
			}
		}
		parseInsnsTag(tag.getCompound("insns"), instructions::add);
		insnPos = Math.max(0, Math.min(instructions.size() - 1, tag.getInt("stackpos")));
		markDirty();
	}
}
