package carpetclient.mixins;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/*
Mixing override to remove client side timeouts.
 */
@Mixin(targets="net.minecraft.network.NetworkManager$5")
public class MixinNetworkManager$5 {

    @Shadow(aliases="val$networkmanager") private @Final NetworkManager field_179248_a;

    @Overwrite
    protected void initChannel(Channel p_initChannel_1_) throws Exception {
        try
        {
            p_initChannel_1_.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
        }
        catch (ChannelException var3)
        {
            ;
        }

        p_initChannel_1_.pipeline().addLast("timeout", new ReadTimeoutHandler(0))
                .addLast("splitter", new NettyVarint21FrameDecoder())
                .addLast("decoder", new NettyPacketDecoder(EnumPacketDirection.CLIENTBOUND))
                .addLast("prepender", new NettyVarint21FrameEncoder())
                .addLast("encoder", new NettyPacketEncoder(EnumPacketDirection.SERVERBOUND))
                .addLast("packet_handler", field_179248_a);
    }
}