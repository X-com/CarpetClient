package carpetclient.util;

import net.minecraft.tileentity.TileEntity;

public interface ITileEntityPiston
{
    void setCarriedBlockEntity(TileEntity blockEntity);
    TileEntity getCarriedBlockEntity();
}
