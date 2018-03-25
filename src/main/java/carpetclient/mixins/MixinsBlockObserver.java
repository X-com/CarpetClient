package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
Mixen class to make observers properly rotate without visual glitches when doing "accurateBlockPlacement".
 */
@Mixin(BlockObserver.class)
public class MixinsBlockObserver extends BlockDirectional {

    protected MixinsBlockObserver(Material materialIn) {
        super(materialIn);
    }

    @Overwrite
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        // rotate observer based on hotkeys
        if (Config.accurateBlockPlacement) {
            if (!Keyboard.isKeyDown(Hotkeys.toggleBlockFacing.getKeyCode())) {
                facing = EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite();
            }
            if (Keyboard.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode())) {
                facing = facing.getOpposite();
            }
        } else {
            facing = EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite();
        }
        return this.getDefaultState().withProperty(FACING, facing);
    }
}
