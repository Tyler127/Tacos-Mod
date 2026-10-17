package tacosmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

public class BeefTacoItem extends TacoItem {
    private static final Consumable CONSUMABLE_COMPONENT =
        createBaseConsumable(TacoNegativeEffectSeverity.NORMAL)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 30 * 20, AMPLIFIER),
                    new MobEffectInstance(MobEffects.SPEED, 30 * 20, AMPLIFIER),
                    new MobEffectInstance(MobEffects.STRENGTH, 30 * 20, AMPLIFIER)
                ),
                1.0F
            ))
            .build();

    public BeefTacoItem(Properties properties) {
        super(properties, 13, 14, CONSUMABLE_COMPONENT);
    }
}
