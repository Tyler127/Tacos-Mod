package tacosmod.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import tacosmod.ModDamageTypes;

public class TwentyThreeFlavorsMobEffect extends InstantaneousMobEffect {
    @FunctionalInterface
    private interface FlavorAction {
        // Returns false if the flavor can't apply to this target, so another one is picked
        boolean apply(ServerLevel level, LivingEntity target, Entity source);
    }

    private record Flavor(int weight, FlavorAction action) {}

    private static final int COMMON = 10;
    private static final int RARE = 1;

    private static final int THIRTY_SECONDS = 30 * 20;
    private static final int TEN_SECONDS = 10 * 20;

    // One of these 23 flavors is picked at random each time the drink is consumed
    private static final List<Flavor> FLAVORS = List.of(
        // Positive flavors
        effect(MobEffects.SPEED, THIRTY_SECONDS, 1),
        effect(MobEffects.HASTE, THIRTY_SECONDS, 1),
        effect(MobEffects.STRENGTH, THIRTY_SECONDS, 0),
        effect(MobEffects.REGENERATION, THIRTY_SECONDS, 0),
        effect(MobEffects.RESISTANCE, THIRTY_SECONDS, 0),
        effect(MobEffects.FIRE_RESISTANCE, THIRTY_SECONDS, 0),
        effect(MobEffects.NIGHT_VISION, THIRTY_SECONDS, 0),
        effect(MobEffects.ABSORPTION, THIRTY_SECONDS, 1),
        playerOnly(COMMON, (level, player) -> give(level, player, new ItemStack(Items.ANCIENT_DEBRIS))),
        playerOnly(COMMON, (level, player) -> {
            player.giveExperienceLevels(10);
            return true;
        }),
        playerOnly(COMMON, TwentyThreeFlavorsMobEffect::teleportToNearestPlayer),
        playerOnly(COMMON, (level, player) -> give(level, player, randomEnchantedBook(level, player.getRandom()))),

        // Negative flavors
        effect(MobEffects.SLOWNESS, THIRTY_SECONDS, 0),
        effect(MobEffects.NAUSEA, TEN_SECONDS, 0),
        effect(MobEffects.BLINDNESS, TEN_SECONDS, 0),
        effect(MobEffects.POISON, TEN_SECONDS, 0),
        effect(MobEffects.LEVITATION, 5 * 20, 0),
        effect(MobEffects.DARKNESS, TEN_SECONDS, 0),
        new Flavor(COMMON, (level, target, source) -> teleportThroughPortal(level, target, (Portal) Blocks.END_PORTAL, Level.END)),
        new Flavor(COMMON, (level, target, source) -> teleportThroughPortal(level, target, (Portal) Blocks.NETHER_PORTAL, Level.NETHER)),
        new Flavor(COMMON, (level, target, source) -> strikeWithLightning(level, target)),
        playerOnly(COMMON, (level, player) -> spawnPhantoms(level, player, 20)),
        new Flavor(RARE, (level, target, source) -> target.hurtServer(level, drPepperDamage(level), Float.MAX_VALUE))
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
        // Used by drinking, splash, and lingering potions
        applyRandomFlavor(level, target, source);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // Used by tipped arrows and /effect. This runs while the entity's effects are being
        // iterated, so defer the flavor until afterward since it may add effects or teleport
        MinecraftServer server = level.getServer();
        server.schedule(server.wrapRunnable(() -> {
            if (entity.isAlive() && entity.level() instanceof ServerLevel currentLevel) {
                applyRandomFlavor(currentLevel, entity, entity);
            }
        }));

        // Remove this effect right away so it only triggers once, even if it was given a long
        // duration or carried over to a copy of the entity after a dimension teleport
        return false;
    }

    private static void applyRandomFlavor(ServerLevel level, LivingEntity target, Entity source) {
        RandomSource random = target.getRandom();
        List<Flavor> remaining = new ArrayList<>(FLAVORS);

        // Keep picking until a flavor applies (e.g. skip "teleport to the Nether" when already there)
        while (!remaining.isEmpty()) {
            Flavor flavor = pickWeighted(remaining, random);
            if (flavor.action().apply(level, target, source)) {
                return;
            }
            remaining.remove(flavor);
        }
    }

    private static Flavor pickWeighted(List<Flavor> flavors, RandomSource random) {
        int totalWeight = flavors.stream().mapToInt(Flavor::weight).sum();
        int roll = random.nextInt(totalWeight);
        for (Flavor flavor : flavors) {
            roll -= flavor.weight();
            if (roll < 0) {
                return flavor;
            }
        }
        return flavors.getLast();
    }

    private static Flavor effect(Holder<MobEffect> effect, int durationTicks, int amplifier) {
        return new Flavor(COMMON, (level, target, source) ->
            target.addEffect(new MobEffectInstance(effect, durationTicks, amplifier), source)
        );
    }

    private static Flavor playerOnly(int weight, BiFunction<ServerLevel, ServerPlayer, Boolean> action) {
        return new Flavor(weight, (level, target, source) ->
            target instanceof ServerPlayer player && action.apply(level, player)
        );
    }

    private static DamageSource drPepperDamage(ServerLevel level) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModDamageTypes.DR_PEPPER));
    }

    private static boolean give(ServerLevel level, ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.spawnAtLocation(level, stack);
        }
        return true;
    }

    private static ItemStack randomEnchantedBook(ServerLevel level, RandomSource random) {
        List<Holder.Reference<Enchantment>> enchantments = level.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .listElements()
            .filter(enchantment -> !enchantment.is(EnchantmentTags.CURSE))
            .toList();

        Holder<Enchantment> enchantment = enchantments.get(random.nextInt(enchantments.size()));
        int enchantmentLevel = Mth.nextInt(random, enchantment.value().getMinLevel(), enchantment.value().getMaxLevel());
        return EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, enchantmentLevel));
    }

    private static boolean teleportToNearestPlayer(ServerLevel level, ServerPlayer player) {
        // Prefer the nearest player in the same dimension, otherwise anyone online
        Optional<ServerPlayer> nearest = level.players().stream()
            .filter(other -> other != player && !other.isSpectator())
            .min(Comparator.comparingDouble(other -> other.distanceToSqr(player)))
            .or(() -> level.getServer().getPlayerList().getPlayers().stream()
                .filter(other -> other != player && !other.isSpectator())
                .findAny());

        if (nearest.isEmpty()) {
            return false;
        }

        ServerPlayer other = nearest.get();
        player.teleport(new TeleportTransition(
            other.level(),
            other.position(),
            Vec3.ZERO,
            other.getYRot(),
            other.getXRot(),
            TeleportTransition.PLAY_PORTAL_SOUND
        ));
        return true;
    }

    private static boolean teleportThroughPortal(
        ServerLevel level,
        LivingEntity target,
        Portal portal,
        ResourceKey<Level> destination
    ) {
        if (level.dimension() == destination) {
            return false;
        }

        // Reuse the vanilla portal logic so the player lands somewhere safe (and has a way back from the Nether)
        TeleportTransition transition = portal.getPortalDestination(level, target, target.blockPosition());
        if (transition == null) {
            return false;
        }

        // Non-player entities come back as a new copy. Give it a portal cooldown like real portal
        // travel so it doesn't bounce straight back through the portal it lands in
        Entity teleported = target.teleport(transition);
        if (teleported != null) {
            teleported.setPortalCooldown();
        }
        return true;
    }

    private static boolean strikeWithLightning(ServerLevel level, LivingEntity target) {
        LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (lightning == null) {
            return false;
        }

        lightning.snapTo(Vec3.atBottomCenterOf(target.blockPosition()));
        level.addFreshEntity(lightning);
        return true;
    }

    private static boolean spawnPhantoms(ServerLevel level, ServerPlayer player, int count) {
        RandomSource random = player.getRandom();

        for (int i = 0; i < count; i++) {
            Phantom phantom = EntityTypes.PHANTOM.create(level, EntitySpawnReason.MOB_SUMMONED);
            if (phantom == null) {
                continue;
            }

            // Try a few random spots around the player, falling back to right above them
            BlockPos spawnPos = player.blockPosition().above(2);
            for (int attempt = 0; attempt < 10; attempt++) {
                BlockPos candidate = player.blockPosition().offset(
                    Mth.nextInt(random, -6, 6),
                    Mth.nextInt(random, 2, 6),
                    Mth.nextInt(random, -6, 6)
                );
                phantom.snapTo(Vec3.atBottomCenterOf(candidate));
                if (level.noCollision(phantom)) {
                    spawnPos = candidate;
                    break;
                }
            }

            phantom.snapTo(Vec3.atBottomCenterOf(spawnPos));
            phantom.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.MOB_SUMMONED, null);
            level.addFreshEntity(phantom);
        }

        return true;
    }
}
