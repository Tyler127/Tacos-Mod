package tacosmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import tacosmod.ModEffects;

public class SuspiciousTacoItem extends TacoItem {
    public static final Consumable CONSUMABLE_COMPONENT =
        createBaseConsumable(TacoNegativeEffectSeverity.EXTREME, AMPLIFIER, AMBIENT, false)
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(ModEffects.ZOMBIE_DISGUISE_EFFECT, 60 * 20, AMPLIFIER, AMBIENT, false),
                1
            ))
            .build();

    public SuspiciousTacoItem(Properties properties) {
        super(properties, 6, 6, CONSUMABLE_COMPONENT);
    }}
