package tacosmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItemIds {
    public static ResourceKey<Item> create(String name) {
        // Create the item key.
        return ResourceKey.create(Registries.ITEM, TacosMod.id(name));
    }

    public static final ResourceKey<Item> BEEF_TACO = create("beef_taco");
    public static final ResourceKey<Item> DOUGH = create("dough");
    public static final ResourceKey<Item> FLOUR = create("flour");
    public static final ResourceKey<Item> RAW_TORTILLA = create("raw_tortilla");
    public static final ResourceKey<Item> TORTILLA = create("tortilla");

}
