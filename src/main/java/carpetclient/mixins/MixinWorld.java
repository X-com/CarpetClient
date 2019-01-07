package carpetclient.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow public TileEntity getTileEntity(BlockPos pos){return null;}

    @Shadow public abstract IBlockState getBlockState(BlockPos pos);

    @Inject(method = "markTileEntityForRemoval", at = @At("HEAD"))
    private void handleRecipeClickeddDD(TileEntity tileEntityIn, CallbackInfo ci) {
        if(tileEntityIn.getPos().getX() != -985 || tileEntityIn.getPos().getZ() != -269) return;
        System.out.println("tileEntityIn " + tileEntityIn);
    }

    @Inject(method = "removeTileEntity", at = @At("HEAD"))
    private void rem(BlockPos pos, CallbackInfo ci) {
//        if(pos.getX() != -985 || pos.getZ() != -269) return;
        TileEntity tileentity = getTileEntity(pos);
        System.out.println("pos " + tileentity);
        if(tileentity != null && tileentity instanceof TileEntityPiston  && ((IMixinTileEntityPiston)tileentity).getProgress() < 1.0){
            System.out.println("---------- found " + pos + " " + ((IMixinTileEntityPiston)tileentity).getProgress());
            System.out.println(getBlockState(pos).getBlock());
            int iter = 1;

            Thread.dumpStack();
            if(((IMixinTileEntityPiston)tileentity).getProgress() == 0.0) iter = 2;

            System.out.println(Minecraft.getMinecraft().player.posX);
        }
    }
}
