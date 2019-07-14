package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {
    /**
     * Allows creative players to no clip
     * @param player
     * @return
     */
    @Redirect(method = "onUpdate()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    private boolean updateNoClipping(EntityPlayer player) {
        return player.isSpectator() || (Config.creativeModeNoClip && player.isCreative() && player.capabilities.isFlying);
    }
}
