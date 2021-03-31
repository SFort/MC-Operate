package com.example.examplemod;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Spoon extends Item {
    public Spoon()
    {
        super(new Item.Properties().group(ItemGroup.MISC));
        setRegistryName("examplemod", "wood_spoon");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        tryCraft(context.getWorld(),context.getPos());
        return ActionResultType.SUCCESS;
    }

    public void tryCraft(World world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        if (state.getProperties().contains(BlockStateProperties.LIT)) {
            world.setBlockState(pos, state.with(BlockStateProperties.LIT, true),0);
        }
    }
}