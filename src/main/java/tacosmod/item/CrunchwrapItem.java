package tacosmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

public class CrunchwrapItem extends TacoItem {
    private static final Consumable CONSUMABLE_COMPONENT =
        createBaseConsumable(TacoNegativeEffectSeverity.NORMAL)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.RESISTANCE, 30 * 20, 3),
                    new MobEffectInstance(MobEffects.SPEED, 30 * 20, 2),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 30 * 20, 2)
                ),
                1.0F
            ))
            .build();

    public CrunchwrapItem(Properties properties) {
        super(properties, 13, 14, CONSUMABLE_COMPONENT);
    }
}
