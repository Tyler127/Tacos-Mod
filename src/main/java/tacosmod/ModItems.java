package tacosmod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import tacosmod.item.BeefTacoItem;

import java.util.function.Function;

public class ModItems {
    public static final Item BAJA_BLAST_SYRUP = register(
        ModItemIds.BAJA_BLAST_SYRUP,
        BeefTacoItem::new,
        new Item.Properties()
    );

    public static final Item BEEF_TACO = register(
        ModItemIds.BEEF_TACO,
        BeefTacoItem::new,
        new Item.Properties()
    );

    public static final Item DOUGH = register(
        ModItemIds.DOUGH,
        Item::new,
        new Item.Properties()
    );

    public static final Item FLOUR = register(
        ModItemIds.FLOUR,
        Item::new,
        new Item.Properties()
    );

    public static final Item RAW_TORTILLA = register(
        ModItemIds.RAW_TORTILLA,
        Item::new,
        new Item.Properties()
    );

    public static final Item TORTILLA = register(
        ModItemIds.TORTILLA,
        Item::new,
        new Item.Properties()
    );

    public static void initialize() {
        // Get the event for modifying entries in the ingredients group.
        // And register an event handler that adds the item to the ingredients group.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
            .register((creativeTab) -> {
                creativeTab.accept(ModItems.BAJA_BLAST_SYRUP);
                creativeTab.accept(ModItems.BEEF_TACO);
                creativeTab.accept(ModItems.DOUGH);
                creativeTab.accept(ModItems.FLOUR);
                creativeTab.accept(ModItems.RAW_TORTILLA);
                creativeTab.accept(ModItems.TORTILLA);
            });
    }

    public static Item register(
        ResourceKey<Item> itemKey,
        Function<Item.Properties, Item> itemFactory,
        Item.Properties settings
    ) {
        // Create the item instance.
        Item item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }
}
