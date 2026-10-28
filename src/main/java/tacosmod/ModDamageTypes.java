package tacosmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    // Defined in data/tacos-mod/damage_type
    public static final ResourceKey<DamageType> DR_PEPPER = create("dr_pepper");

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, TacosMod.id(name));
    }
}
