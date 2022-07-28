package tf.ssf.sfort.operate;


import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static tf.ssf.sfort.operate.client.McClient.mc;

public class BitStak extends Block implements BlockEntityProvider{
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static final DirectionProperty FACING = Properties.FACING;
	public static final ImmutableMap<Item, Predicate<BitStakEntity>> VALID_INSNS;
	static {
		ImmutableMap.Builder<Item, Predicate<BitStakEntity>> bldr = new ImmutableMap.Builder<>();
		bldr.put(Items.REDSTONE, BitStakEntity::computeAdd);
		bldr.put(Items.STICK, BitStakEntity::computeSub);
		bldr.put(Items.GOLD_NUGGET, BitStakEntity::computeGreater);
		bldr.put(Items.IRON_NUGGET, BitStakEntity::computeLesser);
		bldr.put(Items.LAPIS_LAZULI, BitStakEntity::computeDiv);
		bldr.put(Items.BONE, BitStakEntity::computeMul);
		bldr.put(Items.HONEY_BOTTLE, BitStakEntity::computeAnd);
		bldr.put(Items.FURNACE, BitStakEntity::computeXor);
		bldr.put(Items.REDSTONE_TORCH, BitStakEntity::computeNot);
		bldr.put(Items.TORCH, BitStakEntity::computeEquals);
		bldr.put(Items.EGG, BitStakEntity::computeDup);
		bldr.put(Items.GUNPOWDER, BitStakEntity::computePop);
		bldr.put(Items.CLOCK, BitStakEntity::computeTick);

		VALID_INSNS = bldr.build();
	}
	public static Block BLOCK;
	public BitStak() {
		super(Settings.of(Material.PISTON).strength(1.5F));
		setDefaultState(stateManager.getDefaultState().with(POWERED, false).with(FACING, Direction.NORTH));
	}
	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING).add(POWERED);
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (world.isClient) return null;
		if (type == BitStakEntity.ENTITY_TYPE) return (world1, pos1, state1, entity) -> {
			if (entity instanceof BitStakEntity) {
				((BitStakEntity) entity).serverTick(world1, pos1, state1);
			}
		};
		return null;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction horizon = ctx.getPlayerFacing();
		return this.getDefaultState().with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos())).with(FACING, horizon.getOpposite());
	}
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		boolean pow = world.isReceivingRedstonePower(pos);
		if (pow != state.get(POWERED)) {
			world.setBlockState(pos, state.with(POWERED, pow));
		}
	}
	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return false;
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof BitStakEntity)
				((BitStakEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (!world.isClient) {
			BitStakEntity e = (BitStakEntity) world.getBlockEntity(blockPos);
			if(e!=null && !blockState.get(POWERED)) {
				ItemStack stack = player.getStackInHand(hand);
				if (VALID_INSNS.containsKey(stack.getItem())) {
					e.pushInv(stack.split(1));
					e.markDirty();
					return ActionResult.SUCCESS;
				}
			}
		}
		return ActionResult.FAIL;
	}
	public BitStak(Settings settings) {super(settings);}
	public static void register() {
		if (Config.bit != null) {
			BLOCK = Registry.register(Registry.BLOCK, Main.id("bit"), new BitStak());
			BitStakEntity.register();
			if (Config.bit){
				Spoon.SpoonDo craft = (world, pos, cpos, state, cstate) -> {
					world.removeBlock(pos, false);
					if (world instanceof ServerWorld) {
						((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
						world.playSound(null, pos, Spoon.BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
					}
					Vec3i vec = pos.subtract(cpos);
					int x = Integer.compare(vec.getX(), 0);
					int z = x == 0 ? vec.getZ() : 0;
					state = BitStak.BLOCK.getDefaultState();
					if (x != 0 || z != 0) {
						state = state.with(FACING, Direction.fromVector(x, 0, z));
					}
					world.setBlockState(cpos, state);
				};
				Spoon.CRAFT.put(new Pair<>(Blocks.SCULK, Blocks.REDSTONE_BLOCK), craft);
				Spoon.CRAFT.put(new Pair<>(Blocks.REDSTONE_BLOCK, Blocks.SCULK), craft);
			}
		}
	}
	@Override public Item asItem(){return Items.SCULK;}
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new BitStakEntity(pos, state); }
}
class BitStakEntity extends BlockEntity {
	public static BlockEntityType<BitStakEntity> ENTITY_TYPE;
	public static int MAX_INSN = 256;

	public List<Item> instructions = new ArrayList<>();
	public int[] stack = {0,0,0,0,0,0};
	public int stackPos = -1;
	public int insnPos = -1;
	public int redstone = 0;
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("bit"), FabricBlockEntityTypeBuilder.create(BitStakEntity::new, BitStak.BLOCK).build(null));
	}
	public BitStakEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState state) {
		super(blockEntityType, blockPos, state);
	}
	public BitStakEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}

	public void serverTick(World world, BlockPos pos, BlockState state) {
		if (instructions.isEmpty()) return;
		insnPos++;
		if (insnPos>=instructions.size() || insnPos<0) return;
		Item itmInsn = instructions.get(insnPos);
		if (itmInsn == null || itmInsn == Items.AIR){
			criticalErr();
			return;
		}
		Predicate<BitStakEntity> insn = BitStak.VALID_INSNS.get(itmInsn);
		if (insn == null) {
			criticalErr();
			return;
		}
		if (!insn.test(this)){
			criticalErr();
		}
	}
	public void criticalErr(){

	}

	public void dropInv(){
		if (world == null) return;
		instructions.sort(Comparator.comparing(Registry.ITEM::getId));
		for (Item item : instructions) {
			world.spawnEntity(new ItemEntity(world, pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5, item.getDefaultStack()));
		}
		instructions.clear();
	}
	public void pushInv(ItemStack item){
		if (instructions.size() < MAX_INSN) {
			instructions.add(item.getItem());
		} else {
			dropItem(item);
		}
	}
	public void dropItem(ItemStack item){
		if (!item.isEmpty() && world != null)
			world.spawnEntity(new ItemEntity(world, pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5, item));
	}
	public boolean computeAdd(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos] + stack[--stackPos];
		return true;
	}
	public boolean computeSub(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos-1] - stack[stackPos--];
		return true;
	}
	public boolean computeGreater(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos-1] > stack[stackPos--] ? 0 : -1;
		return true;
	}
	public boolean computeLesser(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos-1] < stack[stackPos--] ? 0 : -1;
		return true;
	}
	public boolean computeDiv(){
		if (stackPos<1) return false;
		if (stack[stackPos] == 0) return false;
		stack[stackPos] = stack[stackPos-1] / stack[stackPos--];
		return true;
	}
	public boolean computeMul(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos] * stack[--stackPos];
		return true;
	}
	public boolean computeAnd(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos] & stack[--stackPos];
		return true;
	}
	public boolean computeXor(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos] ^ stack[--stackPos];
		return true;
	}
	public boolean computeNot(){
		if (stackPos<0) return false;
		stack[stackPos] = stack[stackPos] == 0 ? -1 : 0;
		return true;
	}
	public boolean computeEquals(){
		if (stackPos<1) return false;
		stack[stackPos] = stack[stackPos] ^ stack[--stackPos];
		return true;
	}
	public boolean computeDup(){
		if (stackPos<0) return false;
		if (stackPos>=stack.length) return false;
		stack[++stackPos] = stack[stackPos];
		return true;
	}
	public boolean computePop(){
		if (stackPos<0) return false;
		stack[stackPos--] = 0;
		return true;
	}
	public boolean computeTick() {
		if (stackPos<0) return false;
		if (stack[stackPos] <=0){
			stack[stackPos--] = 0;
		} else {
			stack[stackPos]--;
		}
		insnPos--;
		return true;
	}
	public boolean computeIf0(){
		if (stackPos<0) return false;
		if (stack[stackPos] != 0) insnPos++;
		stack[stackPos--] = 0;
		return true;
	}
	public boolean computeSwap(){
		if (stackPos<1) return false;
		int tmp = stack[stackPos];
		stack[stackPos] = stack[stackPos--];
		stack[stackPos] = tmp;
		return true;
	}
	public boolean computeShiftLeft(){
		if (stackPos<1) return false;
		stack[stackPos-1] = stack[stackPos-1] << stack[stackPos];
		stack[stackPos--] = 0;
		return true;
	}
	public boolean computeShiftRight(){
		if (stackPos<1) return false;
		stack[stackPos-1] = stack[stackPos-1] >> stack[stackPos];
		stack[stackPos--] = 0;
		return true;
	}
	public boolean createFunc()
	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putInt("stackPos", stackPos);
		tag.putInt("insnPos", insnPos);
		tag.putInt("redstone", redstone);
		NbtCompound stak = new NbtCompound();
		for (int i=0; i<stack.length; i++) {
			stak.putInt(Integer.toString(i), stack[i]);
		}
		tag.put("stack", stak);
		stak = new NbtCompound();
		for (Item item : instructions) {
			stak.putString(Integer.toString(stak.getSize()), Registry.ITEM.getId(item).toString());
		}
		tag.put("insns", stak);
	}
	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		stackPos = Math.max(0, Math.min(15, tag.getInt("stackPos")));
		redstone = Math.max(0, Math.min(15, tag.getInt("redstone")));
		NbtCompound stak = tag.getCompound("stack");
		for (int i=0; i<stack.length; i++) {
			NbtElement nbt = stak.get(Integer.toString(i));
			if (nbt instanceof NbtInt) {
				stack[i] = ((NbtInt)nbt).intValue();
			}
		}
		stak = tag.getCompound("insns");
		for (String key : stak.getKeys()) {
			NbtElement nbt = stak.get(key);
			if (nbt instanceof NbtString) {
				Item item = Registry.ITEM.get(new Identifier(nbt.asString()));
				if (item != Items.AIR) {
					instructions.add(item);
				}
			}
		}
		insnPos = Math.max(0, Math.min(instructions.size()-1, tag.getInt("stackpos")));
		markDirty();
	}
}

class BitStakRenderer {
	private static final int RENDER_DISTANCE = MathHelper.square(32);
	private int step = 0;
	public static void register(){
		if (Config.fancyInv == null || Config.bit == null) return;
		BlockEntityRendererRegistry.register(BitStakEntity.ENTITY_TYPE, ctx -> new BitStakRenderer()::render);
	}

	public static ImmutableMap<Direction, Vec3d> mapTransform;
	static {
		ImmutableMap.Builder<Direction, Vec3d> bldr = new ImmutableMap.Builder<>();
		bldr.put(Direction.EAST, new Vec3d(1.01, 1, 1));
		bldr.put(Direction.SOUTH, new Vec3d(0, 1, 1.01));
		bldr.put(Direction.NORTH, new Vec3d(1, 1, -.01));
		bldr.put(Direction.WEST, new Vec3d(-.01, 1, 0));
		mapTransform = bldr.build();
	}

	public void render(BitStakEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		BlockState state = entity.getCachedState();
		if (!state.get(BitStak.POWERED)) return;
		MinecraftClient minecraftClient = MinecraftClient.getInstance();
		ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
		if (clientPlayerEntity == null)	return;
		if (!(minecraftClient.options.getPerspective().isFirstPerson() && clientPlayerEntity.isUsingSpyglass()))
			if (clientPlayerEntity.squaredDistanceTo(Vec3d.ofCenter(entity.getPos())) > RENDER_DISTANCE) return;
		Direction dir = clientPlayerEntity.getHorizontalFacing();
		Direction face = state.get(BitStak.FACING);
		if (face.equals(dir)) return;
		matrix.push();
		Vec3d tr = mapTransform.get(face);
		if (tr != null)
			matrix.translate(tr.getX(), tr.getY(), tr.getZ());
		matrix.push();
		matrix.scale(0.015F, -0.015F, 0.015F);

		float rot = face.asRotation();
		if (face.getAxis().equals(Direction.Axis.X))
			rot+=180;
		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rot));

		mc.textRenderer.draw("Text", 8, 8, 0xffff4010, true, matrix.peek().getPositionMatrix(), vertex, false, 0, 15728880);
		if (!entity.instructions.isEmpty()) {
			mc.getItemRenderer().renderItem(entity.instructions.get(step).getDefaultStack(), ModelTransformation.Mode.GROUND, 15728880, overlay, matrix, vertex, 1);
			if (step < entity.instructions.size()) {
				step++;
			} else {
				step = 0;
			}
		}
		matrix.scale(10, 10, 10);
		matrix.pop();
		matrix.pop();


	}
}