package tf.ssf.sfort.operate.tube;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import tf.ssf.sfort.operate.Main;

import java.util.Collections;

public class ColorTubeEntity extends BlockEntity {
    public static BlockEntityType<ColorTubeEntity> ENTITY_TYPE;
    public long colorLvl = 0;
    public TubeConnectTypes[] sides = Collections.nCopies(Direction.values().length, TubeConnectTypes.NONE).toArray(new TubeConnectTypes[0]);

    public static void register() {
        ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Main.id("color_tube"), FabricBlockEntityTypeBuilder.create(ColorTubeEntity::new, ColorTube.BLOCK).build(null));
    }

    public ColorTubeEntity(BlockPos blockPos, BlockState state) {
        super(ENTITY_TYPE, blockPos, state);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (world != null && !world.isClient()) {
            ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putLong("colorLvl", colorLvl);
        for (int i = 0; i < sides.length; i++) {
            tag.putString(Direction.values()[i].getName(), sides[i].name);
        }
        return tag;
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putLong("colorLvl", colorLvl);
        for (int i = 0; i < sides.length; i++) {
            tag.putString(Direction.values()[i].getName(), sides[i].name);
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        colorLvl = tag.getLong("colorLvl");
        for (int i = 0; i < sides.length; i++) {
            TubeConnectTypes con = TubeConnectTypes.nameMap.get(tag.getString(Direction.values()[i].getName()));
            if (con != null) sides[i] = con;
        }
        markDirty();
    }

    public boolean wrenchSide(Direction side) {
        sides[side.ordinal()] = sides[side.ordinal()] == TubeConnectTypes.NONE ? TubeConnectTypes.ALL : TubeConnectTypes.NONE;
        markDirty();
        if (world != null)
            world.updateNeighbor(pos.offset(side), ColorTube.BLOCK, pos);
        return hasRedstoneConnections();
    }

    public boolean setColor(Direction side, TubeConnectTypes clr) {
        sides[side.ordinal()] = clr;
        markDirty();
        if (world != null)
            world.updateNeighbor(pos.offset(side), ColorTube.BLOCK, pos);
        return hasRedstoneConnections();
    }

    public int getWeakPower(Direction dir) {
        TubeConnectTypes clr = sides[dir.ordinal()];
        if (clr.color != null) return clr.getStrength(colorLvl);
        return 0;
    }

    public boolean hasRedstoneConnections() {
        for (TubeConnectTypes side : sides) if (side.color != null) return true;
        return false;
    }

    public void justPlaced() {
        if (world == null) return;
        boolean updated = false;
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; i < sides.length; i++) {
            Direction direction = Direction.values()[i];
            mutable.set(pos).move(direction);
            BlockEntity e = world.getBlockEntity(mutable);
            if (e instanceof ColorTubeEntity) {
                sides[i] = TubeConnectTypes.ALL;
                BlockState blockState = world.getBlockState(mutable);
                if (blockState.isOf(ColorTube.BLOCK)) {
                    boolean enabled = ((ColorTubeEntity) e).setColor(direction.getOpposite(), TubeConnectTypes.ALL);
                    if (enabled != blockState.get(ColorTube.ENABLED))
                        world.setBlockState(mutable, blockState.with(ColorTube.ENABLED, enabled));
                }
                updated = true;
            }
        }
        if (updated) markDirty();
    }

    public void subCompressedColor(int color) {
        for (TubeConnectTypes con : TubeConnectTypes.values()) {
            if ((color & con.compOne) != 0) {
                if ((colorLvl & con.mask) == 0) continue;
                colorLvl -= con.one;
            }
        }
        markDirty();
        updateColorNeighbours();
    }

    public void addCompressedColor(int color) {
        for (TubeConnectTypes con : TubeConnectTypes.values()) {
            if ((color & con.compOne) != 0) {
                if ((colorLvl & con.mask) == con.mask) continue;
                colorLvl += con.one;
            }
        }
        markDirty();
        updateColorNeighbours();
    }

    public void updateColorNeighbours() {
        assert world != null;
        for (int i = 0; i < sides.length; i++) {
            TubeConnectTypes con = sides[i];
            if (con.color == null) continue;
            world.updateNeighbor(pos.offset(Direction.values()[i]), ColorTube.BLOCK, pos);
        }
    }

    public boolean hasAllColorConnection(Direction dir) {
        return sides[dir.ordinal()] == TubeConnectTypes.ALL;
    }
}
