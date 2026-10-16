package datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import tacosmod.ModBlocks;

public class TacosModBlockLootTableProvider extends FabricBlockLootSubProvider {
    public TacosModBlockLootTableProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        super(output, registries);
    }

    @Override
    public void generate() {
        add(ModBlocks.SUGAR_BLOCK, LootTable.lootTable()
            .withPool(applyExplosionCondition(
                ModBlocks.SUGAR_BLOCK,
                LootPool.lootPool()
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(Items.SUGAR)
                            .when(LootItemRandomChanceCondition.randomChance(0.5f))
                            .apply(SetItemCountFunction.setCount(
                                ContextIntProviders.exactly(9)
                            )),
                        LootItem.lootTableItem(ModBlocks.SUGAR_BLOCK)
                    ))
            ))
        );
    }

}
