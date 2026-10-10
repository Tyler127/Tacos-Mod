package tacosmod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {
    public static final Block SUGAR_BLOCK = register(
        ModBlockItemIds.SUGAR_BLOCK,
        Block::new,
        BlockBehaviour.Properties
            .ofFullCopy(Blocks.SAND)
            .sound(SoundType.SAND)
    );

    public static final Block TEST_BLOCK = register(
        ModBlockIds.TEST_BLOCK,
        Block::new,
        BlockBehaviour.Properties.of()
    );

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
            .register(creativeTab -> {
                creativeTab.accept(SUGAR_BLOCK.asItem());
            });
    }

    // Register a block and its item
    private static Block register(
        BlockItemId id,
        Function<BlockBehaviour.Properties, Block> blockFactory,
        BlockBehaviour.Properties properties
    ) {
        // Register the block itself.
        Block block = register(id.block(), blockFactory, properties);

        // Register the corresponding inventory item.
        BlockItem blockItem = new BlockItem(
            block,
            new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(id.item())
        );

        Registry.register(
            BuiltInRegistries.ITEM,
            id.item(),
            blockItem
        );

        return block;
    }

    // Register a block
    private static Block register(
        ResourceKey<Block> blockKey,
        Function<BlockBehaviour.Properties, Block> blockFactory,
        BlockBehaviour.Properties properties
    ) {
        Block block = blockFactory.apply(properties.setId(blockKey));

        return Registry.register(
            BuiltInRegistries.BLOCK,
            blockKey,
            block
        );
    }
}
