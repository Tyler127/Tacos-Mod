package tacosmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItemIds {
    public static final ResourceKey<Item> BAJA_BLAST_SYRUP = create("baja_blast_syrup");
    public static final ResourceKey<Item> BEEF_TACO = create("beef_taco");
    public static final ResourceKey<Item> CHICKEN_TACO = create("chicken_taco");
    public static final ResourceKey<Item> CRUNCHWRAP = create("crunchwrap");
    public static final ResourceKey<Item> DOUGH = create("dough");
    public static final ResourceKey<Item> FISH_TACO = create("fish_taco");
    public static final ResourceKey<Item> FLOUR = create("flour");
    public static final ResourceKey<Item> GROUND_BEEF = create("ground_beef");
    public static final ResourceKey<Item> PORK_TACO = create("pork_taco");
    public static final ResourceKey<Item> RAW_TORTILLA = create("raw_tortilla");
    public static final ResourceKey<Item> SHREDDED_CHICKEN = create("shredded_chicken");
    public static final ResourceKey<Item> PULLED_PORK = create("pulled_pork");
    public static final ResourceKey<Item> SUSPICIOUS_TACO = create("suspicious_taco");
    public static final ResourceKey<Item> TORTILLA = create("tortilla");

    public static ResourceKey<Item> create(String name) {
        // Create the item key
        return ResourceKey.create(Registries.ITEM, TacosMod.id(name));
    }
}
