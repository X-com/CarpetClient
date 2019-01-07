package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.hack.PistonFix;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World {

    protected MixinWorldClient(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Inject(method = "invalidateRegionAndSetBlock", at = @At("HEAD"), cancellable = true)
    private void fixPistonBlinking(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if(Config.clipThroughPistons && state.getBlock() == Blocks.PISTON_EXTENSION) {
            cir.setReturnValue(true);
        }else if(this.getBlockState(pos).getBlock() == Blocks.PISTON_EXTENSION){
            PistonFix.clearBlockLocation(pos);
        }
    }
}
