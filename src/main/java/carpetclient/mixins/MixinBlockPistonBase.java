package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
Mixen class 
1.to make piston/sticky-piston properly rotate without visual glitches when doing "accurateBlockPlacement".
2.ghost block fix for sticky pistons
 */
@Mixin(BlockPistonBase.class)
public abstract class MixinBlockPistonBase extends BlockDirectional {

    @Shadow
    private void checkForMove(World worldIn, BlockPos pos, IBlockState state) {
    }

    @Shadow
    public static @Final
    PropertyBool EXTENDED;

    @Shadow
    private boolean doMove(World worldIn, BlockPos pos, EnumFacing direction, boolean extending) {
        return false;
    }

    protected MixinBlockPistonBase(Material materialIn) {
        super(materialIn);
    }

    // Override this method to comment out a useless line.
    @Inject(method = "onBlockPlacedBy", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, CallbackInfo ci) {
        if (!worldIn.isRemote) {
            this.checkForMove(worldIn, pos, state);
        }
        ci.cancel();
    }

    // Override to fix a client side visual affect when placing blocks in a different orientation.
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
        cir.setReturnValue(this.getDefaultState().withProperty(FACING, facing).withProperty(EXTENDED, Boolean.valueOf(false)));
    }

    // ghost block fix
    @Redirect(method = "eventReceived", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockPistonBase;doMove(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z", ordinal = 1))
    public boolean eventReceivedMixins(BlockPistonBase obj,
                                       World worldIn1, BlockPos pos1, EnumFacing enumfacing, boolean extending, // from doMove
                                       IBlockState state, World worldIn2, BlockPos pos2, int id, int param // from eventReceived
    ) {
        // adding the meta check here and make sure the client only grabs blocks if the block in front isn't
        // a moving block on the server even if its regular blocks that can be pulled on the client. both client 
        // and server should behave the same by forcing the client to ignore blocks if the server can't pull the block in front.
        if ((param & 16) == 0) {
            return this.doMove(worldIn1, pos1, enumfacing, false);
        }

        return false;
    }

    // ghost block fix
    @Redirect(method = "checkForMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", ordinal = 1))
    public void eventReceivedMixins(World worldIn1,
                                    BlockPos pos1, Block blockIn, int eventID, int eventParam, // from addBlockEvent
                                    World worldIn2, BlockPos pos2, IBlockState state // from checkForMove
    ) {
        worldIn1.addBlockEvent(pos1, this, 1, eventParam | ignoreMovingBlockMeta(worldIn1, pos1, EnumFacing.getFront(eventParam)));
    }

    /*
     * This if statement checks if the the pulling block (block that is 2 blocks infront of the extended piston)
     * is a non-moving block and returns a meta value of 16 so it can tell the client to ignore pulling blocks
     * even if the client can pull them.
     */
    private int ignoreMovingBlockMeta(World worldIn, BlockPos pos, EnumFacing enumfacing) {
        BlockPos blockpos = pos.add(enumfacing.getFrontOffsetX() * 2, enumfacing.getFrontOffsetY() * 2, enumfacing.getFrontOffsetZ() * 2);
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block == Blocks.PISTON_EXTENSION) return 16;

        return 0;
    }

}
