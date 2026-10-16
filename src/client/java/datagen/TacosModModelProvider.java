package datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;

import net.minecraft.client.data.models.model.ModelTemplates;
import tacosmod.ModBlocks;
import tacosmod.ModItems;

public class TacosModModelProvider extends FabricModelProvider {
    public TacosModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {
        generator.createTrivialCube(ModBlocks.SUGAR_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        generator.generateFlatItem(ModItems.BAJA_BLAST_SYRUP, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ModItems.BEEF_TACO, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ModItems.CRUNCHWRAP, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ModItems.DOUGH, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ModItems.FLOUR, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ModItems.GROUND_BEEF, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ModItems.RAW_TORTILLA, ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(ModItems.TORTILLA, ModelTemplates.FLAT_ITEM);

    }

    @Override
    public String getName() {
        return "TacosModModelProvider";
    }
}
