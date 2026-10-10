package tacosmod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public class ModPotions {
    public static final Holder<Potion> BAJA_BLAST_POTION =
        Registry.registerForHolder(
            BuiltInRegistries.POTION,
            ModPotionIds.BAJA_BLAST_POTION,
            new Potion(
                "baja_blast_potion",
                new MobEffectInstance(
                    ModEffects.FLIGHT_EFFECT,
                     5 * 60 * 20,
                    0
                )
            )
        );

    public static void initialize() {}

}
