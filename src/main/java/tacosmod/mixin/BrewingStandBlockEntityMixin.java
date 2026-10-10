package tacosmod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tacosmod.ModItems;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Shadow
    private int brewTime;

    @Inject(
        method = "serverTick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;brewTime:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 2,
            shift = At.Shift.AFTER
        )
    )
    private static void tacosmod$slowBajaBlast(
        ServerLevel level,
        BlockPos pos,
        BlockState state,
        BrewingStandBlockEntity entity,
        CallbackInfo ci
    ) {
        ItemStack reagent = entity.getItem(3);
        int tenMinutes = 12000;
        if (reagent.is(ModItems.BAJA_BLAST_SYRUP)) {
            ((BrewingStandBlockEntityMixin) (Object) entity).brewTime = tenMinutes;
        }
    }
}
