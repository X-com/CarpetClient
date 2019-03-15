package carpetclient.mixins;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import carpetclient.Config;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerSP.class, priority = 999)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD",
            target = "Lnet/minecraft/network/play/client/CPacketEntityAction$Action;"
                    + "START_FALL_FLYING:"
                    + "Lnet/minecraft/network/play/client/CPacketEntityAction$Action;"))
    public void onElytraDeploy(CallbackInfo ci) {
        if (Config.elytraFix)
            setFlag(7, true);
    }

    /**
     * Clientside fix for player clipping through other players.
     * @return
     */
    public boolean canBePushed()
    {
        return Config.playerCollisions;
    }

    /**
     * Fixes when going into elytra and cliping into roofs.
     * @param pos
     * @param cir
     */
    @Inject(method = "isOpenBlockSpace", at = @At("HEAD"), cancellable = true)
    private void adjustIsOpenBlockSpace(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!this.world.getBlockState(pos).isNormalCube());
    }
}
