package tacosmod;

import net.minecraft.resources.Identifier;
import net.minecraft.references.BlockItemId;

public class ModBlockItemIds {
    public static final BlockItemId SUGAR_BLOCK = create("sugar_block");

    private static BlockItemId create(String name) {
        Identifier id = TacosMod.id(name);
        return BlockItemId.create(id, id);
    }
}
