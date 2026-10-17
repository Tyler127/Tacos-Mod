
package tacosmod.creative;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import tacosmod.ModItems;
import tacosmod.TacosMod;

public class TacosModCreativeTab {
    public static final ResourceKey<CreativeModeTab> TACOS_TAB_KEY =
        ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            TacosMod.id("tacos")
        );

    public static final CreativeModeTab TACOS_TAB =
        FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.BEEF_TACO))
            .title(Component.translatable("itemGroup.tacos-mod.tacos"))
            .displayItems((params, output) -> {
                for (var item : ModItems.TACOS_MOD_ITEMS) {
                    output.accept(item);
                }
            })
            .build();

    public static void initialize() {
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            TACOS_TAB_KEY,
            TACOS_TAB
        );
    }
}
