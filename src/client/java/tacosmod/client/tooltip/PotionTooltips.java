package tacosmod.client.tooltip;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;
import tacosmod.ModPotions;

public final class PotionTooltips {
    private PotionTooltips() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

            if (contents == null || contents.potion().isEmpty()) {
                return;
            }

            if (contents.potion().get().equals(ModPotions.BAJA_BLAST_POTION)) {
                lines.add(Component.translatable("tooltip.tacos-mod.baja_blast_potion.line1"));
                lines.add(Component.translatable("tooltip.tacos-mod.baja_blast_potion.line2"));
            }
        });
    }
}
