package tacosmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

public class FishTacoItem extends TacoItem {
    private static final Consumable CONSUMABLE_COMPONENT =
        createBaseConsumable(TacoNegativeEffectSeverity.NORMAL)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.LUCK, 2 * 60 * 20, AMPLIFIER),
                    new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 30 * 20, AMPLIFIER)
                ),
                1.0F
            ))
            .build();

    public FishTacoItem(Properties properties) {
        super(properties, 13, 14, CONSUMABLE_COMPONENT);
    }
}
