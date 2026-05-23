package com.oreradar;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class OreColors {
    public static float[] getColor(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)
            return new float[]{0.0f, 1.0f, 1.0f};
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)
            return new float[]{0.0f, 1.0f, 0.0f};
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)
            return new float[]{1.0f, 1.0f, 0.0f};
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)
            return new float[]{1.0f, 0.7f, 0.4f};
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)
            return new float[]{0.8f, 0.4f, 0.2f};
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)
            return new float[]{1.0f, 0.0f, 0.0f};
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)
            return new float[]{0.2f, 0.2f, 1.0f};
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)
            return new float[]{0.3f, 0.3f, 0.3f};
        if (block == Blocks.NETHER_QUARTZ_ORE)
            return new float[]{1.0f, 1.0f, 1.0f};
        if (block == Blocks.ANCIENT_DEBRIS)
            return new float[]{0.8f, 0.3f, 0.8f};
        return null;
    }

    public static boolean isContainer(Block block) {
        return block == Blocks.CHEST
            || block == Blocks.TRAPPED_CHEST
            || block == Blocks.ENDER_CHEST
            || block == Blocks.BARREL
            || block == Blocks.SHULKER_BOX
            || block == Blocks.WHITE_SHULKER_BOX
            || block == Blocks.ORANGE_SHULKER_BOX
            || block == Blocks.MAGENTA_SHULKER_BOX
            || block == Blocks.LIGHT_BLUE_SHULKER_BOX
            || block == Blocks.YELLOW_SHULKER_BOX
            || block == Blocks.LIME_SHULKER_BOX
            || block == Blocks.PINK_SHULKER_BOX
            || block == Blocks.GRAY_SHULKER_BOX
            || block == Blocks.LIGHT_GRAY_SHULKER_BOX
            || block == Blocks.CYAN_SHULKER_BOX
            || block == Blocks.PURPLE_SHULKER_BOX
            || block == Blocks.BLUE_SHULKER_BOX
            || block == Blocks.BROWN_SHULKER_BOX
            || block == Blocks.GREEN_SHULKER_BOX
            || block == Blocks.RED_SHULKER_BOX
            || block == Blocks.BLACK_SHULKER_BOX
            || block == Blocks.FURNACE
            || block == Blocks.BLAST_FURNACE
            || block == Blocks.SMOKER
            || block == Blocks.HOPPER
            || block == Blocks.DROPPER
            || block == Blocks.DISPENSER;
    }
}
