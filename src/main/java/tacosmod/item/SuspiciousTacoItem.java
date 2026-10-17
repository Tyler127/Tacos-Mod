package tacosmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import tacosmod.ModEffects;

public class SuspiciousTacoItem extends Item {
    public static final FoodProperties FOOD_PROPERTIES =
        new FoodProperties.Builder()
            .alwaysEdible()
            .nutrition(6)
            .saturationModifier(6)
            .build();

    static final int AMPLIFIER = 0;
    static final boolean AMBIENT = false;
    static final boolean VISIBLE = false;

    public static final Consumable CONSUMABLE_COMPONENT =
        Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(ModEffects.ZOMBIE_DISGUISE_EFFECT, 60 * 20, AMPLIFIER, AMBIENT, VISIBLE),
                1
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.HUNGER, 60 * 20, AMPLIFIER, AMBIENT, VISIBLE),
                0.80F
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.NAUSEA, 20 * 20, AMPLIFIER, AMBIENT, VISIBLE),
                0.50F
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.POISON, 10 * 20, AMPLIFIER, AMBIENT, VISIBLE),
                0.20F
            ))
            .build();

    public SuspiciousTacoItem(Properties properties) {
        super(properties.food(FOOD_PROPERTIES, CONSUMABLE_COMPONENT));
    }
}
