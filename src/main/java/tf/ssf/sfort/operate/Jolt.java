package tf.ssf.sfort.operate;


import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Collection;

import static tf.ssf.sfort.operate.client.McClient.mc;

public class Jolt extends Block implements BlockEntityProvider {
	public static Block BLOCK;
	public Jolt() {
		super(Settings.of(Material.STONE, MapColor.BLACK).requiresTool().strength(50.0F, Blocks.OBSIDIAN.getBlastResistance()));
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
			if (Config.jolt && Config.obsDispenser != null)
				Spoon.CRAFT.put(new Pair<>(Blocks.SOUL_SAND, Blocks.OBSIDIAN), (world, pos, cpos, state, cstate) -> {
					world.removeBlock(pos, false);
					if (world instanceof ServerWorld) {
						((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.3, 0.15, 0.3, 0.01);
						world.playSound(null, pos, SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.6F);
						world.playSound(null, pos, Spoon.BREAK, SoundCategory.BLOCKS, 0.17F, world.getRandom().nextFloat() * 0.1F + 0.9F);
					}
					world.setBlockState(cpos, Jolt.BLOCK.getDefaultState());
				});
		}
	}
	@Override public Item asItem(){ return Items.DISPENSER; };
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new JoltEntity(pos, state); }
}
class JoltEntity extends BlockEntity implements Inventory {
	public static BlockEntityType<JoltEntity> ENTITY_TYPE;
	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("jolt"), FabricBlockEntityTypeBuilder.create(JoltEntity::new, Jolt.BLOCK).build(null));
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

	public JoltEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState state) { super(blockEntityType, blockPos, state); }
	public JoltEntity(BlockPos blockPos, BlockState state) { super(ENTITY_TYPE, blockPos, state); }
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
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		NbtCompound t = new NbtCompound();
		inv.writeNbt(t);
		tag.put("item",t);
		tag.putByte("dir",dir);
	}
	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		inv = ItemStack.fromNbt(tag.getCompound("item"));
		dir = tag.getByte("dir");
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

		if (this.getWorld() != null && !this.getWorld().isClient()) {
			((ServerWorld) world).getChunkManager().markForUpdate(getPos());
		}
	}
	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = new NbtCompound();
		tag.put("item", inv.writeNbt(new NbtCompound()));
		return tag;
	}

}
class JoltRenderer{
	public static void register(){
		if (Config.fancyInv == null || Config.jolt == null) return;
		BlockEntityRendererRegistry.register(JoltEntity.ENTITY_TYPE, Config.fancyInv ? ctx -> JoltRenderer::render : ctx -> JoltRenderer::look_render);
	}
	public static void render(JoltEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		matrix.push();
		matrix.translate(0.5, 0.75, 0.5);
		mc.getItemRenderer().renderItem(entity.inv, Mode.GROUND, WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()), overlay, matrix, vertex, 1);
		matrix.pop();
	}
	public static void look_render(JoltEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		if(mc.cameraEntity != null && mc.cameraEntity.raycast(8, tickDelta,false).getPos().squaredDistanceTo(entity.getPos().getX()+0.5, entity.getPos().getY()+1, entity.getPos().getZ()+0.5)<0.6)
			render(entity, tickDelta, matrix, vertex, light, overlay);
	}
}