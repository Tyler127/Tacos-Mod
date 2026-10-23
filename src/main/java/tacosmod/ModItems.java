package tacosmod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import tacosmod.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModItems {
    // All tacos-mod items
    public static final List<Item> TACOS_MOD_ITEMS = new ArrayList<>();

    // Items grouped by creative tab
    public static final List<Item> FOOD_DRINK_ITEMS = new ArrayList<>();
    public static final List<Item> INGREDIENT_ITEMS = new ArrayList<>();

    // Food and drink items
    public static final Item BEEF_TACO = register(ModItemIds.BEEF_TACO, BeefTacoItem::new, new Item.Properties(), FOOD_DRINK_ITEMS);
    public static final Item CHICKEN_TACO = register(ModItemIds.CHICKEN_TACO, ChickenTacoItem::new, new Item.Properties(), FOOD_DRINK_ITEMS);
    public static final Item CRUNCHWRAP = register(ModItemIds.CRUNCHWRAP, CrunchwrapItem::new, new Item.Properties(), FOOD_DRINK_ITEMS);
    public static final Item FISH_TACO = register(ModItemIds.FISH_TACO, FishTacoItem::new, new Item.Properties(), FOOD_DRINK_ITEMS);
    public static final Item PORK_TACO = register(ModItemIds.PORK_TACO, PorkTacoItem::new, new Item.Properties(), FOOD_DRINK_ITEMS);
    public static final Item SUSPICIOUS_TACO = register(ModItemIds.SUSPICIOUS_TACO, SuspiciousTacoItem::new, new Item.Properties(), FOOD_DRINK_ITEMS);

    // Ingredient items
    public static final Item BAJA_BLAST_SYRUP = register(ModItemIds.BAJA_BLAST_SYRUP, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item DOUGH = register(ModItemIds.DOUGH, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item DR_PEPPER_SYRUP = register(ModItemIds.DR_PEPPER_SYRUP, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item FLOUR = register(ModItemIds.FLOUR, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item GROUND_BEEF = register(ModItemIds.GROUND_BEEF, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item MOUNTAIN_DEW_SYRUP = register(ModItemIds.MOUNTAIN_DEW_SYRUP, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item RAW_TORTILLA = register(ModItemIds.RAW_TORTILLA, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item SHREDDED_CHICKEN = register(ModItemIds.SHREDDED_CHICKEN, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item PULLED_PORK = register(ModItemIds.PULLED_PORK, Item::new, new Item.Properties(), INGREDIENT_ITEMS);
    public static final Item TORTILLA = register(ModItemIds.TORTILLA, Item::new, new Item.Properties(), INGREDIENT_ITEMS);

    public static void initialize() {
        // Add items to the creative mode tabs
        addItemsToCreativeModeTab(CreativeModeTabs.INGREDIENTS, INGREDIENT_ITEMS);
        addItemsToCreativeModeTab(CreativeModeTabs.FOOD_AND_DRINKS, FOOD_DRINK_ITEMS);
    }

    public static Item register(
        ResourceKey<Item> itemKey,
        Function<Item.Properties, Item> itemFactory,
        Item.Properties settings,
        List<Item> creativeTabItems
    ) {
        // Create the item instance
        Item item = itemFactory.apply(settings.setId(itemKey));

        // Register the item
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        // Add the item to its creative tab
        creativeTabItems.add(item);

        // Add to list of all mod items
        TACOS_MOD_ITEMS.add(item);

        return item;
    }

    private static void addItemsToCreativeModeTab(ResourceKey<CreativeModeTab> tab, List<Item> items) {
        CreativeModeTabEvents.modifyOutputEvent(tab).register(creativeTab -> {
            for (Item item : items) {
                creativeTab.accept(item);
            }
        });
    }
}
