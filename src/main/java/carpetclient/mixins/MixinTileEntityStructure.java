package carpetclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import carpetclient.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;

@Mixin(TileEntityStructure.class)
public abstract class MixinTileEntityStructure extends TileEntity {

    @ModifyConstant(method = "readFromNBT", constant = @Constant(intValue = -32) , expect = 3)
    public int modifyNeg32(int orig) {
        return -Config.structureBlockLimit;
    }

    @ModifyConstant(method = "readFromNBT", constant = @Constant(intValue = 32) , expect = 6)
    public int modify32(int orig) {
        return Config.structureBlockLimit;
    }

}
