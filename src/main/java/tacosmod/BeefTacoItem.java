package tacosmod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

public class BeefTacoItem extends Item {
    public BeefTacoItem(Properties properties) {
        super(properties.food(FOOD_PROPERTIES, CONSUMABLE_COMPONENT));
    }

    public static final FoodProperties FOOD_PROPERTIES =
        new FoodProperties.Builder()
            .alwaysEdible()
            .nutrition(13)
            .saturationModifier(14)
            .build();

    public static final Consumable CONSUMABLE_COMPONENT =
        Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                List.of(
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 10 * 20, 0),
                    new MobEffectInstance(MobEffects.SPEED, 10 * 20, 0),
                    new MobEffectInstance(MobEffects.STRENGTH, 10 * 20, 0)
                ),
                1.0F
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.HUNGER, 10 * 20, 0),
                0.20F
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.NAUSEA, 5 * 20, 0),
                0.075F
            ))
            .onConsume(new ApplyStatusEffectsConsumeEffect(
                new MobEffectInstance(MobEffects.POISON, 5 * 20, 0),
                0.025F
            ))
            .build();
}
