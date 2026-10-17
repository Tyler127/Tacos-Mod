package tacosmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public abstract class TacoItem extends Item {
    protected static final int AMPLIFIER = 0;
    protected static final boolean AMBIENT = false;
    protected static final boolean VISIBLE = true;

    protected TacoItem(Properties properties, int nutrition, float saturation, Consumable consumable) {
        super(properties.food(
            new FoodProperties.Builder()
                .alwaysEdible()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build(),
            consumable
        ));
    }

    protected static Consumable.Builder createBaseConsumable(TacoNegativeEffectSeverity severity) {
        return createBaseConsumable(severity, AMPLIFIER, AMBIENT, VISIBLE);
    }

    protected static Consumable.Builder createBaseConsumable(TacoNegativeEffectSeverity severity, int amplifier) {
        return createBaseConsumable(severity, amplifier, AMBIENT, VISIBLE);
    }

    protected static Consumable.Builder createBaseConsumable(TacoNegativeEffectSeverity severity, int amplifier, boolean ambient) {
        return createBaseConsumable(severity, amplifier, ambient, VISIBLE);
    }

    protected static Consumable.Builder createBaseConsumable(TacoNegativeEffectSeverity severity, int amplifier, boolean ambient, boolean visible) {
        if (severity == TacoNegativeEffectSeverity.NONE) {
            return Consumables.defaultFood();
        }

        return Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.HUNGER, severity.hungerSeconds * 20, amplifier, ambient, visible),
                severity.hungerChance
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.NAUSEA, severity.nauseaSeconds * 20, amplifier, ambient, visible),
                severity.nauseaChance
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.POISON, severity.poisonSeconds * 20, amplifier, ambient, visible),
                severity.poisonChance
            ));
    }
}
