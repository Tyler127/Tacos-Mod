package tacosmod.effect;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class TwentyThreeFlavorsMobEffect extends InstantaneousMobEffect {
    private record Flavor(Holder<MobEffect> effect, int durationTicks, int amplifier) {}

    private static final int THIRTY_SECONDS = 30 * 20;
    private static final int TEN_SECONDS = 10 * 20;

    // One of these 23 flavors is picked at random each time the drink is consumed
    private static final List<Flavor> FLAVORS = List.of(
        // Positive flavors
        new Flavor(MobEffects.SPEED, THIRTY_SECONDS, 1),
        new Flavor(MobEffects.HASTE, THIRTY_SECONDS, 1),
        new Flavor(MobEffects.STRENGTH, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.JUMP_BOOST, THIRTY_SECONDS, 1),
        new Flavor(MobEffects.REGENERATION, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.RESISTANCE, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.FIRE_RESISTANCE, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.WATER_BREATHING, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.INVISIBILITY, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.NIGHT_VISION, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.ABSORPTION, THIRTY_SECONDS, 1),
        new Flavor(MobEffects.SLOW_FALLING, THIRTY_SECONDS, 0),

        // Negative flavors
        new Flavor(MobEffects.SLOWNESS, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.MINING_FATIGUE, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.NAUSEA, TEN_SECONDS, 0),
        new Flavor(MobEffects.BLINDNESS, TEN_SECONDS, 0),
        new Flavor(MobEffects.HUNGER, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.WEAKNESS, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.POISON, TEN_SECONDS, 0),
        new Flavor(MobEffects.LEVITATION, 5 * 20, 0),
        new Flavor(MobEffects.GLOWING, THIRTY_SECONDS, 0),
        new Flavor(MobEffects.DARKNESS, TEN_SECONDS, 0),
        new Flavor(MobEffects.UNLUCK, THIRTY_SECONDS, 0)
    );

    public TwentyThreeFlavorsMobEffect() {
        super(MobEffectCategory.NEUTRAL, 0x6E0F1E);
    }

    @Override
    public void applyInstantaneousEffect(
        ServerLevel level,
        Entity source,
        Entity indirectSource,
        LivingEntity target,
        int amplifier,
        double health
    ) {
        Flavor flavor = FLAVORS.get(target.getRandom().nextInt(FLAVORS.size()));
        target.addEffect(new MobEffectInstance(flavor.effect(), flavor.durationTicks(), flavor.amplifier()), source);
    }
}
