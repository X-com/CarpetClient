package carpetclient.mixins;

import net.minecraft.util.math.Vec3d;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
Injecting code for block rotation. Editing the x value when sending the package "CPacketPlayerTryUseItemOnBlock" to be decoded by carpet.
 */

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    private static final String TARGET = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/Packet;)V";
//    "net/minecraft/client/network/NetHandlerPlayClient.sendPacket(Lnet/minecraft/network/Packet;)V"
//    private static final String TARGET = "Lnet/minecraft/world/border;contains(Lnet/minecraft/util/math/BlockPos;)Z";
//    private static final String TARGET = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/play/client/CPacketPlayerTryUseItemOnBlock;)V";
//    private static final String TARGET = "Lnet/minecraft/world/border;contains(Lnet/minecraft/util/math/BlockPos;)Z";
//    private static final String TARGET = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;f";
//    private static final String TARGET = "f";
    
//    @Inject(method = "processRightClickBlock", at = @At(value = "INVOKE", target = TARGET))
    @Inject(method = "processRightClickBlock", at = @At("HEAD"))
    public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand){
        System.out.println("testing working ");
        return EnumActionResult.SUCCESS;
    }

//    @Inject(method = "syncCurrentPlayItem", at = @At("HEAD"))
//    protected void processRightClickBlockInject(CallbackInfo ci) {
//        System.out.println("testing working ");
//    }
}
