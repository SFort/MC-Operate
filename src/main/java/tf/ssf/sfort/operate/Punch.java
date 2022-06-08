package tf.ssf.sfort.operate;


import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import static tf.ssf.sfort.operate.client.McClient.mc;

public class Punch extends Block implements BlockEntityProvider{
	public static final BooleanProperty POWERED = Properties.POWERED;
	public static Block BLOCK;
	public Punch() {
		super(Settings.of(Material.PISTON).strength(1.5F));
		setDefaultState(stateManager.getDefaultState().with(POWERED, false));
	}
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
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
		return true;
	}
	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof PunchEntity){
			return ((PunchEntity)e).getComparator();
		}
		return 0;
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof PunchEntity)
				((PunchEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (!world.isClient) {
			PunchEntity e = (PunchEntity) world.getBlockEntity(blockPos);
			if(e!=null) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack.isEmpty()) e.popInv();
				else e.pushInv(stack.split(1));
				e.markDirty();
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.CONSUME;
	}
	public Punch(Settings settings) {super(settings);}
	public static void register() {
		if (Config.punch != null) {
			BLOCK = Registry.register(Registry.BLOCK, Main.id("punch"), new Punch());
			PunchEntity.register();
			if (Config.punch)
				Spoon.CRAFT.put(new Pair<>(Blocks.PISTON, Blocks.CRAFTING_TABLE), (world, pos, cpos, state, cstate) -> {
					world.removeBlock(pos, false);
					if (world instanceof ServerWorld) {
						((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
						world.playSound(null, pos, Spoon.BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
					}
					world.setBlockState(cpos, Punch.BLOCK.getDefaultState());
				});
		}
	}
	@Override public Item asItem(){return Items.PISTON;}
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new PunchEntity(pos, state); }
}
class PunchInventory extends CraftingInventory{
	public ItemStack[] inv = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
	public static final int[] sequence = new int[]{0, 3, 1, 4, 6, 7, 2, 5, 8};
	public PunchInventory() {
		super(null, 0, 0);
	}
	public ItemStack popStack(){
		for (int i : sequence)
			if (!inv[i].isEmpty()) {
				ItemStack ret = inv[i];
				inv[i] = ItemStack.EMPTY;
				return ret;
			}
		return ItemStack.EMPTY;
	}
	public ItemStack pushStack(ItemStack item){
		for (int i : sequence)
			if (inv[i].isEmpty()){
				inv[i] = item.copy();
				return ItemStack.EMPTY;
			}
		ItemStack ret = inv[0];
		inv[0] = item;
		return ret;
	}
	public NbtCompound writeNbt(NbtCompound tag) {
		for (int i=0; i<size(); i++)
			if (!inv[i].isEmpty())
				tag.put(String.valueOf(i), inv[i].writeNbt(new NbtCompound()));
		return tag;
	}
	public void readNbt(NbtCompound tag) {
		for (int i=0; i<size(); i++){
			final String si = String.valueOf(i);
			if(tag.contains(si)) {
				inv[i] = ItemStack.fromNbt(tag.getCompound(si));
			} else if (!inv[i].isEmpty()){
				inv[i] = ItemStack.EMPTY;
			}
		}
	}
	@Override
	public void provideRecipeInputs(RecipeMatcher finder) {
		for (ItemStack item : inv)
			finder.addUnenchantedInput(item);
	}
	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : inv)
			if (!itemStack.isEmpty()) return false;
		return true;
	}
	@Override public ItemStack removeStack(int slot) {
		ItemStack ret = inv[slot];
		inv[slot] = ItemStack.EMPTY;
		return ret;
	}
	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return inv[slot].isEmpty();
	}
	@Override public void setStack(int slot, ItemStack stack) {inv[slot] = stack;}
	@Override public ItemStack getStack(int slot) {return inv[slot];}
	@Override public ItemStack removeStack(int slot, int amount) {return removeStack(slot);}
	@Override public int getMaxCountPerStack() {return 1;}
	@Override public boolean canPlayerUse(PlayerEntity player) {return false;}
	@Override public void clear() {Arrays.fill(inv, ItemStack.EMPTY);}
	@Override public int getHeight() {return 3;}
	@Override public int getWidth() {return 3;}
	@Override public int size() {return 9;}
}
class PunchEntity extends BlockEntity implements Inventory {
	public static BlockEntityType<PunchEntity> ENTITY_TYPE;
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("punch"), FabricBlockEntityTypeBuilder.create(PunchEntity::new, Punch.BLOCK).build(null));
	}
	public final PunchInventory inv = new PunchInventory();
	public Optional<CraftingRecipe> craftResult = Optional.empty();
	public ItemStack craftResultDisplay = ItemStack.EMPTY;
	protected PunchEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState state) {
		super(blockEntityType, blockPos, state);
	}
	public PunchEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
	}
	public void dropInv(){
		if (world == null) return;
		for (ItemStack item : inv.inv)
			if (!item.isEmpty())
				world.spawnEntity(new ItemEntity(world, pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,item));
	}
	public int getComparator(){
		int i=0;
		for (ItemStack item : inv.inv)
			if (!item.isEmpty()) i++;
		if (i==9) return 15;
		return i;
	}
	public void popInv(){
		dropItem(inv.popStack());
	}
	public void pushInv(ItemStack item){
		dropItem(inv.pushStack(item));
	}
	public void dropItem(ItemStack item){
		if (!item.isEmpty() && world != null)
			world.spawnEntity(new ItemEntity(world, pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5, item));
	}
	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.put("items", inv.writeNbt(new NbtCompound()));
	}
	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		inv.readNbt(tag.getCompound("items"));
		markDirty();
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(
				this,
				b -> toInitialChunkDataNbt()
		);
	}
	@Override
	public void markDirty() {
		super.markDirty();
		if (this.world != null) {
			craftResult = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inv, world);
			if (!this.world.isClient()) {
				((ServerWorld) world).getChunkManager().markForUpdate(getPos());
			} else {
				if (craftResult.isPresent()) craftResultDisplay = craftResult.get().getOutput();
				else if (!craftResultDisplay.isEmpty()) craftResultDisplay = ItemStack.EMPTY;
			}
		}
	}
	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = new NbtCompound();
		tag.put("items", inv.writeNbt(new NbtCompound()));
		return tag;
	}
	public boolean canCraft(){
		return craftResult.isPresent() && this.world != null && !this.getCachedState().get(Punch.POWERED);
	}
	@Override
	public ItemStack getStack(int slot) {
		if (this.canCraft()){
			return craftResult.get().getOutput();
		}
		return inv.getStack(PunchInventory.sequence[slot]);
	}
	@Override
	public ItemStack removeStack(int slot) {
		if(this.canCraft()){
			ItemStack ret = craftResult.get().craft(inv);
			inv.clear();
			world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
			return ret;
		}
		return inv.removeStack(PunchInventory.sequence[slot]);
	}
	@Override public void setStack(int slot, ItemStack stack) {
		if(this.canCraft()){
			craftResult = Optional.empty();
			return;
		}
		inv.setStack(PunchInventory.sequence[slot], stack);
	}
	@Override public boolean isValid(int slot, ItemStack stack) {
		if(this.canCraft()) return false;
		return inv.isValid(PunchInventory.sequence[slot], stack);
	}
	@Override public boolean canPlayerUse(PlayerEntity player) {return inv.canPlayerUse(player);}
	@Override public void clear() {inv.clear();}
	@Override public int getMaxCountPerStack() {return inv.getMaxCountPerStack();}
	@Override public int size() {return inv.size();}
	@Override public boolean isEmpty() {return inv.isEmpty();}
	@Override public ItemStack removeStack(int slot, int amount) {return removeStack(slot);}

}

class PunchRenderer{
	public static void register(){
		if (Config.fancyInv == null || Config.punch == null) return;
		BlockEntityRendererRegistry.register(PunchEntity.ENTITY_TYPE, Config.fancyInv ? ctx -> PunchRenderer::render : ctx -> PunchRenderer::look_render);
	}
	public static void render(PunchEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		if (!(entity.craftResultDisplay.isEmpty() || entity.getCachedState().get(Punch.POWERED))) {
			matrix.push();
			matrix.translate(0.5, 1, 0.5);
			mc.getItemRenderer().renderItem(entity.craftResultDisplay, ModelTransformation.Mode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, 1);
			matrix.pop();
		}
		renderSide(entity, tickDelta, matrix, vertex, light, overlay, m -> {});
		renderSide(entity, tickDelta, matrix, vertex, light, overlay,
				m -> {
			m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
			m.translate(-1, 0, 0);
		});
		renderSide(entity, tickDelta, matrix, vertex, light, overlay,
				m -> {
					m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270));
					m.translate(0, 0, -1);
		});
		renderSide(entity, tickDelta, matrix, vertex, light, overlay,
				m -> {
					m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
					m.translate(-1, 0, -1);
		});
	}
	public static void renderSide(PunchEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay, Consumer<MatrixStack> applyDirection) {
		for (int i = 0; i < entity.inv.inv.length; i++) {
			ItemStack item = entity.inv.inv[i];
			matrix.push();
			applyDirection.accept(matrix);
			matrix.translate(0.28 + (i % 3) * 0.22, 0.61 - (i / 3) * 0.22, 0.95);
			matrix.push();
			matrix.scale(0.6f, 0.6f, 0.6f);
			if (!item.isEmpty())
				mc.getItemRenderer().renderItem(item, ModelTransformation.Mode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, 1);
			matrix.pop();
			matrix.pop();
		}
	}
	public static void look_render(PunchEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		Vec3d pos = mc.cameraEntity.raycast(8, tickDelta,false).getPos();
		if(mc.cameraEntity != null)
			if (pos.squaredDistanceTo(entity.getPos().getX()+0.5, entity.getPos().getY()+1, entity.getPos().getZ()+0.5)<0.6 || pos.squaredDistanceTo(entity.getPos().getX()+0.5, entity.getPos().getY()+0.5, entity.getPos().getZ()+0.5)<0.6){
				int dir = mc.cameraEntity.getHorizontalFacing().getId();
				if (dir>1){
					if (!(entity.craftResultDisplay.isEmpty() || entity.getCachedState().get(Punch.POWERED))) {
						matrix.push();
						matrix.translate(0.5, 1, 0.5);
						mc.getItemRenderer().renderItem(entity.craftResultDisplay, ModelTransformation.Mode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, 1);
						matrix.pop();
					}
					final Consumer<MatrixStack> rot = switch (dir){
						case 3 -> m -> {
							m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
							m.translate(-1, 0, -1);
						};
						case 4 -> m -> {
							m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
							m.translate(-1, 0, 0);
						};
						case 5 -> m -> {
							m.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270));
							m.translate(0, 0, -1);
						};
						default -> m -> {};
					};
					renderSide(entity, tickDelta, matrix, vertex, light, overlay, rot);

				} else {
					render(entity, tickDelta, matrix, vertex, light, overlay);
				}
			}
	}
}