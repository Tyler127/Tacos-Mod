package tacosmod;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

public class ModPotions {
    public static final Holder<Potion> BAJA_BLAST_POTION = register(
        ModPotionIds.BAJA_BLAST_POTION,
        "baja_blast_potion",
        new MobEffectInstance(ModEffects.FLIGHT_EFFECT, 3 * 60 * 20, 0)
    );

    public static final Holder<Potion> DR_PEPPER_POTION = register(
        ModPotionIds.DR_PEPPER_POTION,
        "dr_pepper_potion",
        new MobEffectInstance(ModEffects.TWENTY_THREE_FLAVORS_EFFECT, 1, 0)
    );

    // Haste VIII lets an Efficiency V netherite pickaxe instantly mine deepslate
    public static final Holder<Potion> MOUNTAIN_DEW_POTION = register(
        ModPotionIds.MOUNTAIN_DEW_POTION,
        "mountain_dew_potion",
        new MobEffectInstance(MobEffects.HASTE, 3 * 60 * 20, 7)
    );

    public static void initialize() {}

    private static Holder<Potion> register(
            ResourceKey<Potion> potionKey,
            String name,
            MobEffectInstance effect
    ) {
        return Registry.registerForHolder(
            BuiltInRegistries.POTION,
            potionKey,
            new Potion(name, effect)
        );
    }
}
