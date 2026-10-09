package tacosmod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    public static void initialize() {
        // Get the event for modifying entries in the ingredients group.
        // And register an event handler that adds the item to the ingredients group.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register((creativeTab) -> creativeTab.accept(ModItems.BEEF_TACO));
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

    public static final Item BEEF_TACO = register(
            ModItemIds.BEEF_TACO,
            Item::new,
            new Item.Properties()
    );

}
