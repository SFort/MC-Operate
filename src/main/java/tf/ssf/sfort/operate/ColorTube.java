package tf.ssf.sfort.operate;


import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

enum ConnectTypes {
	WHITE("white", DyeColor.WHITE),
	ORANGE("orange", DyeColor.ORANGE),
	MAGENTA("magenta", DyeColor.MAGENTA),
	LIGHT_BLUE("light_blue", DyeColor.LIGHT_BLUE),
	YELLOW("yellow", DyeColor.YELLOW),
	LIME("lime", DyeColor.LIME),
	PINK("pink", DyeColor.PINK),
	GREY("grey", DyeColor.GRAY),
	LIGHT_GREY("light_grey", DyeColor.LIGHT_GRAY),
	CYAN("cyan", DyeColor.CYAN),
	PURPLE("purple", DyeColor.PURPLE),
	BLUE("blue", DyeColor.BLUE),
	BROWN("brown", DyeColor.BROWN),
	GREEN("green", DyeColor.GREEN),
	RED("red", DyeColor.RED),
	BLACK("black", DyeColor.BLACK),
	NONE("none", 0),
	ALL("all", 0);
	final long one;
	final long mask;
	final DyeColor color;
	final String name;
	public static final ImmutableMap<Item, ConnectTypes> itemMap;
	public static final ImmutableMap<String, ConnectTypes> nameMap;
	static {
		ImmutableMap.Builder<Item, ConnectTypes> bldr = new ImmutableMap.Builder<>();
		bldr.put(Items.WHITE_DYE, WHITE);
		bldr.put(Items.ORANGE_DYE, ORANGE);
		bldr.put(Items.MAGENTA_DYE, MAGENTA);
		bldr.put(Items.LIGHT_BLUE_DYE, LIGHT_BLUE);
		bldr.put(Items.YELLOW_DYE, YELLOW);
		bldr.put(Items.LIME_DYE, LIME);
		bldr.put(Items.PINK_DYE, PINK);
		bldr.put(Items.GRAY_DYE, GREY);
		bldr.put(Items.LIGHT_GRAY_DYE, LIGHT_GREY);
		bldr.put(Items.CYAN_DYE, CYAN);
		bldr.put(Items.PURPLE_DYE, PURPLE);
		bldr.put(Items.BLUE_DYE, BLUE);
		bldr.put(Items.BROWN_DYE, BROWN);
		bldr.put(Items.GREEN_DYE, GREEN);
		bldr.put(Items.RED_DYE, RED);
		bldr.put(Items.BLACK_DYE, BLACK);
		itemMap = bldr.build();
		ImmutableMap.Builder<String, ConnectTypes> nameBldr = new ImmutableMap.Builder<>();
		for (ConnectTypes con : ConnectTypes.values()) {
			nameBldr.put(con.name, con);
		}
		nameMap = nameBldr.build();
	}
	ConnectTypes(String name, long mask) {
		this.one = 0;
		this.mask = mask;
		this.color = null;
		this.name = name;
	}
	ConnectTypes(String name, DyeColor color) {
		one = 1L<<(ordinal()*4);
		mask = one + one<<1 + one<<2 + one<<3;
		this.color = color;
		this.name = name;
	}

	public ConnectTypes next() {
		return values()[ordinal()+1 >= ConnectTypes.values().length ? 0 : ordinal()+1];
	}
}

public class ColorTube extends Block implements BlockEntityProvider, Spoonable {
	public static final BooleanProperty ENABLED = Properties.ENABLED;
	public static final VoxelShape collisionShape =  Block.createCuboidShape(4,4,4,12,12,12);
	public static Block BLOCK;
	public ColorTube() {
		super(Settings.of(Material.PISTON).nonOpaque().strength(.5f));
		setDefaultState(stateManager.getDefaultState().with(ENABLED, false));
	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ENABLED);
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return collisionShape;
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return collisionShape;
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		if (world.isAir(fromPos)) {
			BlockEntity entity = world.getBlockEntity(pos);
			if (entity instanceof ColorTubeEntity) {
				Direction direction = Direction.fromVector(fromPos.subtract(pos));
				if (direction != null) {
					boolean enabled = ((ColorTubeEntity) entity).setColor(direction, ConnectTypes.NONE);
					if (enabled != state.get(ENABLED)) world.setBlockState(pos, state.with(ENABLED, enabled));
				}
			}
		}
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}
	@Override
	public ActionResult operate$onUse(BlockState blockState, World world, BlockPos blockPos, ItemUsageContext blockHitResult) {
		if (!world.isClient) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e instanceof ColorTubeEntity && blockState.isOf(BLOCK)) {
				Direction dir = blockHitResult.getSide();
				boolean enabled = ((ColorTubeEntity)e).wrenchSide(dir);
				if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
				blockPos = blockPos.offset(dir);
				e = world.getBlockEntity(blockPos);
				blockState = world.getBlockState(blockPos);
				if (e instanceof ColorTubeEntity && blockState.isOf(BLOCK)) {
					enabled = ((ColorTubeEntity)e).wrenchSide(dir.getOpposite());
					if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
				}
				return ActionResult.SUCCESS;
			}
		}
		return null;
	}
	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (!world.isClient && blockState.isOf(BLOCK)) {
			BlockEntity e = world.getBlockEntity(blockPos);
			if(e instanceof ColorTubeEntity) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack !=null) {
					ConnectTypes clr = ConnectTypes.itemMap.get(stack.getItem());
					if (clr != null) {
						Direction dir = blockHitResult.getSide();
						boolean enabled = ((ColorTubeEntity)e).setColor(dir, clr);
						if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
						blockPos = blockPos.offset(dir);
						e = world.getBlockEntity(blockPos);
						blockState = world.getBlockState(blockPos);
						if (e instanceof ColorTubeEntity && blockState.isOf(BLOCK)) {
							enabled = ((ColorTubeEntity) e).setColor(dir.getOpposite(), clr);
							if (enabled != blockState.get(ENABLED)) world.setBlockState(blockPos, blockState.with(ENABLED, enabled));
						}
						return ActionResult.SUCCESS;
					}
				}
			}
		}
		return ActionResult.PASS;
	}
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		BlockEntity e = world.getBlockEntity(pos);
		if (e instanceof ColorTubeEntity) return ((ColorTubeEntity)e).getWeakPower(direction);
		return 0;
	}
	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return state.get(ENABLED);
	}
	public ColorTube(Settings settings) {super(settings);}
	public static void register() {
		if (Config.colorTube != null) {
			BLOCK = Registry.register(Registry.BLOCK, Main.id("color_tube"), new ColorTube());
			ColorTubeEntity.register();
			if (Config.colorTube) {
				Spoon.PLACE.put(Items.REDSTONE, (context -> {
					World world = context.getWorld();
					PlayerEntity p = context.getPlayer();
					BlockPos gpos = context.getBlockPos().offset(context.getSide());
					if (p != null && world.getBlockState(gpos).isAir()) {
						world.setBlockState(gpos, ColorTube.BLOCK.getDefaultState());
						p.getOffHandStack().decrement(1);
						BlockEntity e = world.getBlockEntity(gpos);
						if (e instanceof ColorTubeEntity) ((ColorTubeEntity)e).justPlaced();
						return true;
					}
					return false;
				}));
			}
		}
	}
	@Override public Item asItem(){return Items.COBBLESTONE;}
	@Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) { return new ColorTubeEntity(pos, state); }

}
class ColorTubeEntity extends BlockEntity {
	public static BlockEntityType<ColorTubeEntity> ENTITY_TYPE;
	public long colorLvl = 0;
	public ConnectTypes[] sides = Collections.nCopies(Direction.values().length, ConnectTypes.NONE).toArray(new ConnectTypes[0]);

	public static void register() {
		ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, Main.id("color_tube"), FabricBlockEntityTypeBuilder.create(ColorTubeEntity::new, ColorTube.BLOCK).build(null));
	}
	public ColorTubeEntity(BlockPos blockPos, BlockState state) {
		super(ENTITY_TYPE, blockPos, state);
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
		for (int i=0; i<sides.length; i++) {
			tag.putString(Direction.values()[i].getName(), sides[i].name);
		}
		return tag;
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.putLong("colorLvl", colorLvl);
		for (int i=0; i<sides.length; i++) {
			tag.putString(Direction.values()[i].getName(), sides[i].name);
		}
	}
	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		colorLvl = tag.getLong("colorLvl");
		for (int i=0; i<sides.length; i++) {
			ConnectTypes con = ConnectTypes.nameMap.get(tag.getString(Direction.values()[i].getName()));
			if (con != null) sides[i] = con;
		}
		markDirty();
	}

	public boolean wrenchSide(Direction side) {
		sides[side.ordinal()] = sides[side.ordinal()] == ConnectTypes.NONE ? ConnectTypes.ALL : ConnectTypes.NONE;
		markDirty();
		return hasRedstoneConnections();
	}

	public boolean setColor(Direction side, ConnectTypes clr) {
		sides[side.ordinal()] = clr;
		markDirty();
		return hasRedstoneConnections();
	}

	public int getWeakPower(Direction dir){
		ConnectTypes clr = sides[dir.ordinal()];
		if (clr.color != null) return (int) ((colorLvl & clr.mask) >>> clr.ordinal());
		return 0;
	}

	public boolean hasRedstoneConnections() {
		for (ConnectTypes side : sides) if (side.color != null) return true;
		return false;
	}

	public void justPlaced() {
		if (world == null) return;
		boolean updated = false;
		BlockPos.Mutable mutable = pos.mutableCopy();
		for (int i=0; i<sides.length; i++) {
			Direction direction = Direction.values()[i];
			mutable.set(pos).move(direction);
			BlockEntity e = world.getBlockEntity(mutable);
			if (e instanceof ColorTubeEntity) {
				sides[i] = ConnectTypes.ALL;
				BlockState blockState = world.getBlockState(mutable);
				if (blockState.isOf(ColorTube.BLOCK)) {
					boolean enabled = ((ColorTubeEntity)e).setColor(direction.getOpposite(), ConnectTypes.ALL);
					if (enabled != blockState.get(ColorTube.ENABLED)) world.setBlockState(mutable, blockState.with(ColorTube.ENABLED, enabled));
				}
				updated = true;
			}
		}
		if (updated) markDirty();
	}
}

class ColorTubeRenderer {
	public static final VoxelShape sideShape =  Block.createCuboidShape(-2,4,-2,2,8,2);

	public static void register(){
		if (Config.colorTube == null) return;
		BlockEntityRendererRegistry.register(ColorTubeEntity.ENTITY_TYPE, ctx -> new ColorTubeRenderer()::render);
	}

	public void render(ColorTubeEntity entity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertex, int light, int overlay) {
		for (int i=0;i<entity.sides.length;i++) {
			ConnectTypes side = entity.sides[i];
			matrix.push();
			matrix.translate(.5, .5, .5);
			matrix.push();
			matrix.multiply(Direction.values()[i].getRotationQuaternion());
			if (side == ConnectTypes.ALL) {
				drawDirectionalLines(matrix, vertex.getBuffer(RenderLayer.LINES), sideShape, 0, 0, 0, 1, 1,1, .3f);
			} else if (side.color != null) {
				int color = side.color.getSignColor();
				drawDirectionalLines(matrix, vertex.getBuffer(RenderLayer.LINES), sideShape, 0, 0, 0, ((color>>16) & 0xff)/255f, ((color>>8) & 0xff)/255f, (color & 0xff)/255f, .9f);
			}
			matrix.pop();
			matrix.pop();
		}
	}
	public static void drawDirectionalLines(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
		List<Box> list = shape.getBoundingBoxes();
		MatrixStack.Entry entry = matrices.peek();
		for (Box box : list) {
			VoxelShapes.cuboid(box.offset(0.0, 0.0, 0.0)).forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
				if (minY == maxY) return;
				float k = (float)(maxX - minX);
				float l = (float)(maxY - minY);
				float m = (float)(maxZ - minZ);
				float n = MathHelper.sqrt(k * k + l * l + m * m);
				k /= n;
				l /= n;
				m /= n;
				vertexConsumer.vertex(entry.getPositionMatrix(), (float)(minX + offsetX), (float)(minY + offsetY), (float)(minZ + offsetZ)).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), k, l, m).next();
				vertexConsumer.vertex(entry.getPositionMatrix(), (float)(maxX + offsetX), (float)(maxY + offsetY), (float)(maxZ + offsetZ)).color(red, green, blue, alpha).normal(entry.getNormalMatrix(), k, l, m).next();
			});
		}
	}
}