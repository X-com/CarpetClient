package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
Mixen class to make observers properly rotate without visual glitches when doing "accurateBlockPlacement".
 */
@Mixin(BlockObserver.class)
public class MixinsBlockObserver extends BlockDirectional {

    protected MixinsBlockObserver(Material materialIn) {
        super(materialIn);
    }

    // Override for placing blocks in the correct orientation when using accurate block placement
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, CallbackInfoReturnable<IBlockState> cir) {
        if (Config.accurateBlockPlacement) {
            if (!Hotkeys.isKeyDown(Hotkeys.toggleBlockFacing.getKeyCode())) {
                facing = EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite();
            }
            if (Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode())) {
                facing = facing.getOpposite();
            }
        } else {
            facing = EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite();
        }
        cir.setReturnValue(this.getDefaultState().withProperty(FACING, facing));
    }
}
