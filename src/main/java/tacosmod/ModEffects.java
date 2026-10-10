package tacosmod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;
import tacosmod.effect.FlightMobEffect;

public class ModEffects {
    public static final Holder<MobEffect> FLIGHT_EFFECT = register(
        ModEffectIds.FLIGHT,
        new FlightMobEffect()
    );

    public static void initialize() {}

    private static Holder<MobEffect> register(
        ResourceKey<MobEffect> effectKey,
        MobEffect effect
    ) {
        return Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            effectKey,
            effect
        );
    }
}
