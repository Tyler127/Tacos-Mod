package tacosmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;

public class ModEffectIds {
    public static final ResourceKey<MobEffect> FLIGHT = create("flight");

    public static ResourceKey<MobEffect> create(String name) {
        return ResourceKey.create(
            Registries.MOB_EFFECT,
            TacosMod.id(name)
        );
    }
}
