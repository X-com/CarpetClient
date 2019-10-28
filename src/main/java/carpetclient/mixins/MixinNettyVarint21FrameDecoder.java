package carpetclient.mixins;

import net.minecraft.network.NettyVarint21FrameDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NettyVarint21FrameDecoder.class)
public class MixinNettyVarint21FrameDecoder {
    @ModifyConstant(method = "decode", constant = @Constant(intValue = 3), remap = false)
    private static int limitExpander(int timeoutSeconds) {
        return 5;
    }
}
