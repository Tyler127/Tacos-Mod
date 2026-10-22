package tacosmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class ChickenTacoItem extends TacoItem {
    private static final Consumable CONSUMABLE_COMPONENT =
        createBaseConsumable(TacoNegativeEffectSeverity.NORMAL)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.HASTE, 30 * 20, 4),
                1.0F
            ))
            .build();

    public ChickenTacoItem(Properties properties) {
        super(properties, 13, 14, CONSUMABLE_COMPONENT);
    }
}
