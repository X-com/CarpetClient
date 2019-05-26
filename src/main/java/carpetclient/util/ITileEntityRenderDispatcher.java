package carpetclient.util;

import net.minecraft.tileentity.TileEntity;

public interface ITileEntityRenderDispatcher
{
    void renderTileEntityOffset(TileEntity tileentityIn, float partialTicks, int destroyStage, double xOffset, double yOffset, double zOffset);
}
