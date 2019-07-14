package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Removes scoreboard on the right, from nessie.
 */
@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void toggleScoreboard(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
        if (Config.isScoreboardHidden.getValue()) {
            ci.cancel();
        }
    }
}
