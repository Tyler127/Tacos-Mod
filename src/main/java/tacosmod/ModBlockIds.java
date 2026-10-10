package tacosmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class ModBlockIds {
    public static final ResourceKey<Block> TEST_BLOCK = create("test_block");

    private static ResourceKey<Block> create(String name) {
        return ResourceKey.create(
            Registries.BLOCK,
            TacosMod.id(name)
        );
    }
}
