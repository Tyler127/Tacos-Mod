package tacosmod.client.mixin;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import tacosmod.client.render.AvatarRenderStateAccess;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements AvatarRenderStateAccess {
    @Unique
    private boolean tacosmod$zombieDisguise;

    @Override
    public boolean tacosmod$hasZombieDisguise() {
        return tacosmod$zombieDisguise;
    }

    @Override
    public void tacosmod$setZombieDisguise(boolean value) {
        this.tacosmod$zombieDisguise = value;
    }
}
