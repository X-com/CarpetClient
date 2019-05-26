package carpetclient.mixins;

import carpetclient.util.ITileEntityRenderDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRenderDispatcher implements ITileEntityRenderDispatcher
{
    @Shadow public double entityX;

    @Shadow public double entityY;

    @Shadow public double entityZ;

    @Shadow public World world;

    @Shadow public static double staticPlayerX;
    @Shadow public static double staticPlayerY;
    @Shadow public static double staticPlayerZ;

    @Shadow public abstract void render(TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float p_192854_10_);

    @Override
    public void renderTileEntityOffset(TileEntity tileentityIn, float partialTicks, int destroyStage, double xOffset, double yOffset, double zOffset)
    {
        if (tileentityIn.getDistanceSq(this.entityX - xOffset, this.entityY - yOffset, this.entityZ - zOffset) < tileentityIn.getMaxRenderDistanceSquared())
        {
            RenderHelper.enableStandardItemLighting();
            int i = this.world.getCombinedLight(tileentityIn.getPos(), 0);
            int j = i % 65536;
            int k = i / 65536;

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos blockpos = tileentityIn.getPos();
            this.render(tileentityIn, (double)blockpos.getX() - staticPlayerX + xOffset, (double)blockpos.getY() - staticPlayerY + yOffset, (double)blockpos.getZ() - staticPlayerZ + zOffset, partialTicks, destroyStage, 1.0F);
        }
    }
}
