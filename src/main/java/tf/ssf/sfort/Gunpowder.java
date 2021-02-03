package tf.ssf.sfort;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Random;

public class Gunpowder extends HorizontalConnectingBlock {
    public final int time = 7;
    public static final BooleanProperty POWERED = Properties.POWERED;
    protected Gunpowder(Settings settings) {
        super(8.0F, 0.0F, 1.0F, 1.0F, 0.0F, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false).with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(WATERLOGGED, false));
    }
    @Override
    public Item asItem(){ return Items.GUNPOWDER; };
    public boolean emitsRedstonePower(BlockState state) { return true; }
    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) { return state.get(POWERED)?15:0; }


    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockState s = world.getBlockState(pos.offset(dir));
            if (s.getBlock() instanceof Gunpowder) ((Gunpowder)s.getBlock()).ignite(world, s, pos.offset(dir));
        }
        //fuze=false;
        world.removeBlock(pos, false);
    }
    public void ignite(WorldAccess world, BlockState state, BlockPos pos){
        if(!state.get(POWERED)){
            world.setBlockState(pos,state.with(POWERED,true),3);
            world.getBlockTickScheduler().schedule(pos,this, time);
        }
    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            if (!player.isCreative()) {
                if (item == Items.FLINT_AND_STEEL) {
                    itemStack.damage(1, player, (playerEntity) -> {
                        playerEntity.sendToolBreakStatus(hand);
                    });
                } else {
                    itemStack.decrement(1);
                }
            }
            ignite(world, state, pos);
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        if (!world.isClient) {
            if (projectile.isOnFire())
                ignite(world, state, hit.getBlockPos());
        }

    }
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        boolean pow = world.isReceivingRedstonePower(pos);
        if(pow) {
            world.getBlockTickScheduler().schedule(pos, this, time);
        }
        return this.getDefaultState().with(POWERED, pow).with(NORTH, !world.isAir(pos.north())).with(SOUTH, !world.isAir(pos.south())).with(WEST, !world.isAir(pos.west())).with(EAST, !world.isAir(pos.east()));
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        //fuze = false;
        boolean pow = newState.getWeakRedstonePower(world,pos,Direction.fromVector(pos.getX()-posFrom.getX(),pos.getY()-posFrom.getY(),pos.getZ()-posFrom.getZ()))!=0;
        if(pow) {
            world.getBlockTickScheduler().schedule(pos, this, time);
            state = state.with(POWERED, true);
        }
        return state.with(FACING_PROPERTIES.get(direction), !newState.isAir());
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED, POWERED);
    }
    public void rainTick(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if(state.get(POWERED))
            world.setBlockState(pos,state.with(POWERED, false),3);
    }
}
