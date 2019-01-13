package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockRedstoneDiode;
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
Mixen class to make comperator properly rotate without visual glitches when doing "accurateBlockPlacement".
 */
@Mixin(BlockRedstoneDiode.class)
public abstract class MixinBlockRedstoneDiode extends BlockHorizontal {

    protected MixinBlockRedstoneDiode(Material materialIn) {
        super(materialIn);
    }

    // Override to fix a client side visual affect when placing blocks in a different orientation.
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void placementState(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, CallbackInfoReturnable<IBlockState> cir) {
        facing = placer.getHorizontalFacing().getOpposite();
        if (Config.accurateBlockPlacement && Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode())) {
            facing = facing.getOpposite();
        }

        cir.setReturnValue(this.getDefaultState().withProperty(FACING, facing));
    }
}
