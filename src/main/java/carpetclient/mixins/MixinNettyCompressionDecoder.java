package carpetclient.mixins;

import net.minecraft.network.NettyCompressionDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NettyCompressionDecoder.class)
public class MixinNettyCompressionDecoder {
    @ModifyConstant(method = "decode", constant = @Constant(intValue = 2097152), remap = false)
    private static int limitExpander(int timeoutSeconds) {
        return Integer.MAX_VALUE;
    }
}
