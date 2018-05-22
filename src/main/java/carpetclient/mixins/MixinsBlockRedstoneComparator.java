package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

/*
Mixen class to make comperator properly rotate without visual glitches when doing "accurateBlockPlacement".
 */
@Mixin(BlockRedstoneComparator.class)
public abstract class MixinsBlockRedstoneComparator extends BlockRedstoneDiode {

    @Shadow
    public static @Final
    PropertyBool POWERED;
    @Shadow
    public static @Final
    PropertyEnum<BlockRedstoneComparator.Mode> MODE;

    protected MixinsBlockRedstoneComparator(boolean powered) {
        super(powered);
    }

    @Overwrite
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        // rotate comperator based on hotkeys
        facing = placer.getHorizontalFacing().getOpposite();
        if (Config.accurateBlockPlacement && Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode())) {
            facing = facing.getOpposite();
        }

        return this.getDefaultState().withProperty(FACING, facing).withProperty(POWERED, Boolean.valueOf(false)).withProperty(MODE, BlockRedstoneComparator.Mode.COMPARE);
    }
}
