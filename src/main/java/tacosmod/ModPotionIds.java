package tacosmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.alchemy.Potion;

public class ModPotionIds {
    public static final ResourceKey<Potion> BAJA_BLAST_POTION = create("baja_blast_potion");

    private static ResourceKey<Potion> create(String name) {
        Identifier id = TacosMod.id(name);
        return ResourceKey.create(Registries.POTION, id);
    }
}
