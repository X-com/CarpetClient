package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends Entity {
    @Shadow public abstract boolean isCreative();

    @Shadow public PlayerCapabilities capabilities;

    public MixinEntityPlayer(World world) {
        super(world);
    }

    /**
     * Allows creative players to no clip
     * @param player
     * @return
     */
    @Redirect(method = "onUpdate()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    private boolean updateNoClipping(EntityPlayer player) {
        return player.isSpectator() || (Config.creativeModeNoClip.getValue() && player.isCreative() && player.capabilities.isFlying);
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (type == MoverType.SELF || !(Config.creativeModeNoClip.getValue() && this.isCreative() && this.capabilities.isFlying)) {
            super.move(type, x, y, z);
        }
    }
}
