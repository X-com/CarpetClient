package carpetclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/*
Mixing override to remove client side timeouts.
 */
@Mixin(targets="net.minecraft.network.NetworkManager$5")
public class MixinNetworkManager$5 {
    @ModifyConstant(method = "initChannel", constant = @Constant(intValue = 30), remap = false)
    private static int noTimeout(int timeoutSeconds) {
        return 0;
    }
}