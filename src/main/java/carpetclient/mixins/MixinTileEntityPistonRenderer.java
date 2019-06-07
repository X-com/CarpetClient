package carpetclient.mixins;

import carpetclient.util.ITileEntityPiston;
import carpetclient.util.ITileEntityRenderDispatcher;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityPistonRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TileEntityPistonRenderer.class)
public abstract class MixinTileEntityPistonRenderer extends TileEntitySpecialRenderer<TileEntityPiston>
{
    @Shadow protected abstract boolean renderStateModel(BlockPos pos, IBlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides);

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityPistonRenderer;renderStateModel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/World;Z)Z",
                    ordinal = 3),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void renderStateModelTE(TileEntityPiston te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci,
                                    BlockPos blockpos, IBlockState iblockstate, Block block, Tessellator tessellator, BufferBuilder bufferbuilder, World world)
    {
        TileEntity carriedTileEntity = ((ITileEntityPiston)te).getCarriedBlockEntity();
        if (carriedTileEntity != null)
        {
            carriedTileEntity.setPos(te.getPos());
            ((ITileEntityRenderDispatcher)TileEntityRendererDispatcher.instance).renderTileEntityOffset(carriedTileEntity, partialTicks, destroyStage, te.getOffsetX(partialTicks), te.getOffsetY(partialTicks), te.getOffsetZ(partialTicks));
        }
    }
}
