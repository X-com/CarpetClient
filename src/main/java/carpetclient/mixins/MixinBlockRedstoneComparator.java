package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
Mixen class to make comperator properly rotate without visual glitches when doing "accurateBlockPlacement".
 */
@Mixin(BlockRedstoneComparator.class)
public abstract class MixinBlockRedstoneComparator extends BlockRedstoneDiode {

    @Shadow
    public static @Final
    PropertyBool POWERED;
    @Shadow
    public static @Final
    PropertyEnum<BlockRedstoneComparator.Mode> MODE;

    protected MixinBlockRedstoneComparator(boolean powered) {
        super(powered);
    }

    // Override for placing blocks in the correct orientation when using accurate block placement
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, CallbackInfoReturnable<IBlockState> cir) {
        facing = placer.getHorizontalFacing().getOpposite();
        if (Config.accurateBlockPlacement && Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode())) {
            facing = facing.getOpposite();
        }

        cir.setReturnValue(this.getDefaultState().withProperty(FACING, facing).withProperty(POWERED, Boolean.valueOf(false)).withProperty(MODE, BlockRedstoneComparator.Mode.COMPARE));
    }
}
