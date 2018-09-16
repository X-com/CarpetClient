package carpetclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import carpetclient.Config;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;

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
    
}
