package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDispenser;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
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

@Mixin(BlockDispenser.class)
public abstract class MixinBlockDispenser extends BlockContainer {

    @Shadow
    public static @Final
    PropertyDirection FACING;

    @Shadow
    public static @Final
    PropertyBool TRIGGERED;

    protected MixinBlockDispenser(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, CallbackInfoReturnable<IBlockState> cir) {
        if (Config.accurateBlockPlacement) {
            if (!Hotkeys.isKeyDown(Hotkeys.toggleBlockFacing.getKeyCode())) {
                facing = EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite();
            }
            if (!Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode())) {
                facing = facing.getOpposite();
            }
        } else {
            facing = EnumFacing.getDirectionFromEntityLiving(pos, placer);
        }
        cir.setReturnValue(this.getDefaultState().withProperty(FACING, facing).withProperty(TRIGGERED, Boolean.valueOf(false)));
    }
}
