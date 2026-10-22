package datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.references.ItemIds;

import tacosmod.TacosMod;

public class TacosModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public static final TagKey<Item> COOKED_FISH = TagKey.create(Registries.ITEM, TacosMod.id("cooked_fish"));

    public TacosModItemTagProvider(
        FabricPackOutput output,
        CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        builder(COOKED_FISH)
            .add(ItemIds.COOKED_COD)
            .add(ItemIds.COOKED_SALMON);
    }
}
