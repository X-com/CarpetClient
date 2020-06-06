package carpetclient.mixins;

import carpetclient.Config;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TileEntityPistonRenderer.class)
public abstract class MixinTileEntityPistonRenderer extends TileEntitySpecialRenderer<TileEntityPiston>
{
    @Shadow protected abstract boolean renderStateModel(BlockPos pos, IBlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides);

    private TileEntityPiston piston;
    private float partialTicks;
    private int destroyStage;

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityPistonRenderer;renderStateModel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/World;Z)Z",
                    ordinal = 3),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void renderStateModelTE(TileEntityPiston te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci,
                                    BlockPos blockpos, IBlockState iblockstate, Block block, Tessellator tessellator, BufferBuilder bufferbuilder, World world)
    {
        if (!Config.movableTileEntities)
            return;

        this.piston = te;
        this.partialTicks = partialTicks;
        this.destroyStage = destroyStage;
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityPistonRenderer;renderStateModel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/world/World;Z)Z",
                    ordinal = 3)
    )
    private boolean renderStateModelTE(TileEntityPistonRenderer renderer, BlockPos pos, IBlockState state, BufferBuilder buffer, World world, boolean checkSides)
    {
        if (Config.movableTileEntities && !((IMixinBufferBuilder) Tessellator.getInstance().getBuffer()).getIsDrawing())
        {
            TileEntity carriedTileEntity = ((ITileEntityPiston) this.piston).getCarriedBlockEntity();
            if (carriedTileEntity != null)
            {
                if (TileEntityRendererDispatcher.instance.getRenderer(carriedTileEntity) == null)
                {
                    return this.renderStateModel(pos, state, buffer, world, checkSides);
                }
                else
                {
                    carriedTileEntity.setPos(this.piston.getPos());
                    ((ITileEntityRenderDispatcher) TileEntityRendererDispatcher.instance).renderTileEntityOffset(carriedTileEntity, this.partialTicks, this.destroyStage, this.piston.getOffsetX(this.partialTicks), this.piston.getOffsetY(this.partialTicks), this.piston.getOffsetZ(this.partialTicks));
                    return true;
                }
            }
        }

        return this.renderStateModel(pos, state, buffer, world, checkSides);
    }
}
