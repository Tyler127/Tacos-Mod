package tacosmod.item;

public enum TacoNegativeEffectSeverity {
    NONE(
        0, 0.0F,
        0, 0.0F,
        0, 0.0F
    ),
    MILD(
        5, 0.05F,
        3, 0.01F,
        2, 0.005F
    ),
    NORMAL(
        10, 0.20F,
        5, 0.075F,
        5, 0.025F
    ),
    SEVERE(
        30, 0.60F,
        20, 0.30F,
        15, 0.15F
    ),
    EXTREME(
        60, 1.0F,
        30, 0.50F,
        20, 0.10F
    );

    public final int hungerSeconds;
    public final float hungerChance;

    public final int nauseaSeconds;
    public final float nauseaChance;

    public final int poisonSeconds;
    public final float poisonChance;

    TacoNegativeEffectSeverity(
        int hungerSeconds, float hungerChance,
        int nauseaSeconds, float nauseaChance,
        int poisonSeconds, float poisonChance
    ) {
        this.hungerSeconds = hungerSeconds;
        this.hungerChance = hungerChance;
        this.nauseaSeconds = nauseaSeconds;
        this.nauseaChance = nauseaChance;
        this.poisonSeconds = poisonSeconds;
        this.poisonChance = poisonChance;
    }
}
