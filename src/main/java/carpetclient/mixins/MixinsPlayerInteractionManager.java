package carpetclient.mixins;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
Mixins class to fix ghost block fix from mining
 */
@Mixin(PlayerInteractionManager.class)
public class MixinsPlayerInteractionManager {
    
    @Shadow public World world;
    @Shadow public EntityPlayerMP player;
    
    /*
    Injection to add block updates for the block that is being miss minsed fixing ghost block mining.
     */
    @Inject(method = "onBlockClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;sendBlockBreakProgress(ILnet/minecraft/util/math/BlockPos;I)V"))
    public void post(BlockPos pos, EnumFacing side, CallbackInfo ci) {
        player.connection.sendPacket(new SPacketBlockChange(world, pos));
    }
}
