package tacosmod.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import tacosmod.ModEffects;
import tacosmod.client.render.AvatarRenderStateAccess;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(
        method = "extractRenderState",
        at = @At("TAIL")
    )
    private void tacosmod$extractZombieDisguise(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        AvatarRenderStateAccess renderState = (AvatarRenderStateAccess) state;
        boolean isDisguised = entity.hasEffect(ModEffects.ZOMBIE_DISGUISE_EFFECT);
        renderState.tacosmod$setZombieDisguise(isDisguised);
    }

    @Inject(
        method = "getTextureLocation",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tacosmod$useZombieTexture(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
        AvatarRenderStateAccess renderState = (AvatarRenderStateAccess) state;
        if (renderState.tacosmod$hasZombieDisguise()) {
            cir.setReturnValue(
                Identifier.fromNamespaceAndPath("tacos-mod", "textures/entity/zombie_skin.png")
            );
        }
    }

    @Unique
    private Identifier tacosmod$zombieHandTexture(Identifier original) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return original;
        }

        if (player.hasEffect(ModEffects.ZOMBIE_DISGUISE_EFFECT)) {
            return Identifier.fromNamespaceAndPath("tacos-mod", "textures/entity/zombie_skin.png");
        }

        return original;
    }

    @ModifyVariable(
        method = "renderRightHand",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private Identifier tacosmod$zombieRightHandTexture(Identifier skinTexture) {
        return tacosmod$zombieHandTexture(skinTexture);
    }

    @ModifyVariable(
        method = "renderLeftHand",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private Identifier tacosmod$zombieLeftHandTexture(Identifier skinTexture) {
        return tacosmod$zombieHandTexture(skinTexture);
    }
}
