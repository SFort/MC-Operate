package tf.ssf.sfort;


import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Objects;

public class Jolt extends Block implements BlockEntityProvider{
	public static Block BLOCK;
	public Jolt() {
		super(Settings.of(Material.STONE, MaterialColor.BLACK).requiresTool().strength(50.0F, Blocks.OBSIDIAN.getBlastResistance()));
	}
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof JoltEntity) {
			BlockPos s = fromPos.subtract(pos);
			((JoltEntity) e).update_dir(Direction.fromVector(s.getX(),s.getY(),s.getZ()));
		}
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity e = world.getBlockEntity(pos);
			if (e instanceof JoltEntity)
				((JoltEntity) e).dropInv();
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient && (stack.isEmpty() || stack.getItem() instanceof BlockItem)) {
			JoltEntity e = (JoltEntity) world.getBlockEntity(blockPos);
			if(e!=null) {
				player.setStackInHand(hand, ItemStack.EMPTY);
				e.replaceStack(stack);
				e.markDirty();
				e.sync();
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.CONSUME;
	}
	public Jolt(Settings settings) { super(settings); }
	public static void register() {
		if (Config.jolt != null) {
			BLOCK = Registry.register(Registry.BLOCK, Main.id("jolt"), new Jolt());
			JoltEntity.register();
		}
	}
	@Override public Item asItem(){ return Items.DISPENSER; };
	@Override public BlockEntity createBlockEntity(BlockView world) { return new JoltEntity(); }
}
class JoltEntity extends BlockEntity implements Inventory, BlockEntityClientSerializable {
	public static BlockEntityType<JoltEntity> ENTITY_TYPE;
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("jolt"), BlockEntityType.Builder.create(JoltEntity::new, Jolt.BLOCK).build(null));
	}
	public ItemStack inv = ItemStack.EMPTY;
	private byte dir = 0;
	public void update_dir(Direction d){
		if(world != null && d !=null) {
			byte b_dir = (byte)(1<<d.getId());
			BlockState state = world.getBlockState(pos.offset(d));
			BlockState placing = Block.getBlockFromItem(inv.getItem()).getDefaultState();
			boolean bl = state.getBlock() instanceof PistonHeadBlock && state.get(Properties.FACING).equals(d.getOpposite());
			BlockPos place_pos = bl?pos.offset(d.getOpposite()):pos.offset(d);
			Runnable place = () -> {
				if(inv != ItemStack.EMPTY && inv.getItem() instanceof BlockItem && world.getBlockState(place_pos).isAir()){
					inv.decrement(1);
					Collection<Property<?>> p = placing.getBlock().getDefaultState().getProperties();
					if (p.contains(Properties.FACING))
						world.setBlockState(place_pos, placing.with(Properties.FACING, d));
					else if (p.contains(Properties.AXIS))
						world.setBlockState(place_pos, placing.with(Properties.AXIS, d.getAxis()));
					else
						world.setBlockState(place_pos, placing);
				}
			};
			if (bl){
				//didn't like the push feature
				//if re enabled in future fix constant place bug
				//place.run();
				dir|=b_dir;
			}else if ((b_dir&dir)!=0){
				dir^=b_dir;
				place.run();
			}
		}else {
			dir = 0;
		}
		markDirty();
	}

	protected JoltEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	public JoltEntity() { super(ENTITY_TYPE); }
	public void dropInv(){ if (world !=null && inv!=ItemStack.EMPTY) world.spawnEntity(new ItemEntity(world, pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,inv.copy())); }
	public void replaceStack(ItemStack item){ dropInv();inv=item; }
	@Override public void setStack(int slot, ItemStack stack) { if(slot==0)inv=stack; }
	@Override public boolean canPlayerUse(PlayerEntity player) { return false; }
	@Override public void clear() { inv=ItemStack.EMPTY; }
	@Override public int size() { return 1; }
	@Override public boolean isEmpty() { return inv == ItemStack.EMPTY; }
	@Override public ItemStack getStack(int slot) { return slot==0? inv: ItemStack.EMPTY; }
	@Override
	public ItemStack removeStack(int slot) {
		if (slot==0){
			ItemStack i = inv;
			clear();
			return i;
		}
		return ItemStack.EMPTY;
	}
	@Override public ItemStack removeStack(int slot, int amount) {
		if (slot==0 && inv!=ItemStack.EMPTY){
			return inv.split(amount);
		}
		return ItemStack.EMPTY;
	}
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		CompoundTag t = new CompoundTag();
		inv.toTag(t);
		tag.put("item",t);
		tag.putByte("dir",dir);
		return tag;
	}
	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		inv = ItemStack.fromTag(tag.getCompound("item"));
		dir = tag.getByte("dir");
	}

	@Override
	public void fromClientTag(CompoundTag tag) { inv = ItemStack.fromTag(tag.getCompound("item")); }

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		CompoundTag t = new CompoundTag();
		inv.toTag(t);
		tag.put("item",t);
		return tag;
	}
}
class JoltRenderer extends BlockEntityRenderer<JoltEntity>{
	public JoltRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}
	public static void register(){
		if (Config.fancyInv != null)
		if(Config.fancyInv)
			BlockEntityRendererRegistry.INSTANCE.register(JoltEntity.ENTITY_TYPE, JoltRenderer::new);
		else
			BlockEntityRendererRegistry.INSTANCE.register(JoltEntity.ENTITY_TYPE, JoltLookRenderer::new);
	}
	@Override
	public void render(JoltEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		matrix.push();
		matrix.translate(0.5, 0.75, 0.5);
		MinecraftClient.getInstance().getItemRenderer().renderItem(entity.inv, Mode.GROUND, WorldRenderer.getLightmapCoordinates(Objects.requireNonNull(entity.getWorld()), entity.getPos().up()), overlay, matrix, vertex);
		matrix.pop();
	}
}
class JoltLookRenderer extends JoltRenderer{
	public JoltLookRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}
	@Override
	public void render(JoltEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		if(MinecraftClient.getInstance().cameraEntity != null && MinecraftClient.getInstance().cameraEntity.raycast(8, tickDelta,false).getPos().squaredDistanceTo(entity.getPos().getX()+0.5, entity.getPos().getY()+1, entity.getPos().getZ()+0.5)<0.6)
			super.render(entity, tickDelta, matrix, vertex, light, overlay);
	}
}