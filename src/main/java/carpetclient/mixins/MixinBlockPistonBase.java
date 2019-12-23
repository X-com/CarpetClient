package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import carpetclient.coders.EDDxample.PistonHelper;
import carpetclient.util.ITileEntityPiston;
import carpetclient.util.IWorldServer;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/*
Mixen class 
1.to make piston/sticky-piston properly rotate without visual glitches when doing "accurateBlockPlacement".
2.ghost block fix for sticky pistons
 */
@Mixin(BlockPistonBase.class)
public abstract class MixinBlockPistonBase extends BlockDirectional {

    private List<TileEntity> tileEntitiesList;

    private BlockPos blockpos; // For movableTE
    
    private int mixinEventParam; // For ghost blocks fix

    @Shadow
    private void checkForMove(World worldIn, BlockPos pos, IBlockState state) {
    }

    @Shadow
    public static @Final
    PropertyBool EXTENDED;

    @Shadow
    private @Final
    boolean isSticky;

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

    /*
     * This if statement checks if the the pulling block (block that is 2 blocks infront of the extended piston)
     * is a non-moving block and returns a meta value of 16 so it can tell the client to ignore pulling blocks
     * even if the client can pull them.
     */
    private int ignoreMovingBlockMeta(World worldIn, BlockPos pos, EnumFacing enumfacing) {
        BlockPos blockpos = pos.add(enumfacing.getXOffset() * 2, enumfacing.getYOffset() * 2, enumfacing.getZOffset() * 2);
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();

        if (block == Blocks.PISTON_EXTENSION) return 16;

        return 0;
    }

//    // Inject into block activated to show piston update order
//    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
//    public void pistonUpdateOrder(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {

    /**
     * Add block activation to get piston update order, code provided by EDDxample.
     *
     * @param worldIn
     * @param pos
     * @param state
     * @param playerIn
     * @param hand
     * @param facing
     * @param hitX
     * @param hitY
     * @param hitZ
     * @return
     */
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!Config.pistonVisualizer.getValue()) return false;

        boolean flag = playerIn.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && playerIn.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.AIR;

        if (worldIn.isRemote && flag) {
            boolean extending = !(Boolean) state.getValue(BlockPistonBase.EXTENDED);
            if ((!pos.equals(PistonHelper.pistonPos) || !PistonHelper.activated) && (extending || isSticky)) {
                PistonHelper.setPistonMovement(worldIn, state, pos, extending);
            } else {
                PistonHelper.activated = false;
            }
            if (worldIn.isRemote) {
                return isSticky || !(Boolean) state.getValue(BlockPistonBase.EXTENDED);
            }
        }

        return flag;
    }

    // MovableTE
    @Redirect(method = "canPush", at = @At(value="INVOKE", target = "Lnet/minecraft/block/Block;hasTileEntity()Z"))
    private static boolean canPushTE(Block block)
    {
        if (!Config.movableTileEntities)
            return block.hasTileEntity();

        if (!block.hasTileEntity())
            return !true;
        else
            return !(isPushableTileEntityBlock(block));
    }

    private static boolean isPushableTileEntityBlock(Block block)
    {
        //Making PISTON_EXTENSION (BlockPistonMoving) pushable would not work as its createNewTileEntity()-method returns null
        return block != Blocks.ENDER_CHEST && block != Blocks.ENCHANTING_TABLE && block != Blocks.END_GATEWAY
                && block != Blocks.END_PORTAL && block != Blocks.MOB_SPAWNER && block != Blocks.PISTON_EXTENSION;
    }

    @Inject(method = "doMove", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Ljava/util/List;size()I", remap = false, ordinal = 4), locals = LocalCapture.CAPTURE_FAILHARD)
    private void doMoveTE(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                          BlockPistonStructureHelper blockpistonstructurehelper, List<BlockPos> list, List<IBlockState> list1,
                          List<BlockPos> list2, int k,  IBlockState[] aiblockstate, EnumFacing enumfacing) {
        doMoveTE(worldIn, pos, direction, extending, cir, blockpistonstructurehelper, list, list1, list2, k, aiblockstate);
    }

    @Surrogate // EnumFacing local var only present in recompiled Minecraft
    private void doMoveTE(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                          BlockPistonStructureHelper blockpistonstructurehelper, List<BlockPos> list, List<IBlockState> list1,
                          List<BlockPos> list2, int k,  IBlockState[] aiblockstate) {
        if (!Config.movableTileEntities)
            return;

        tileEntitiesList = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++)
        {
            BlockPos blockPos = list.get(i);
            TileEntity tileEntity = worldIn.getTileEntity(blockPos);
            tileEntitiesList.add(tileEntity);
            if (tileEntity != null)
            {
                worldIn.removeTileEntity(blockPos);
                tileEntity.markDirty();
            }
        }
    }

    @Inject(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void setTileEntityTE_NoShift(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                                 BlockPistonStructureHelper blockpistonstructurehelper, List<BlockPos> list, List<IBlockState> list1,
                                 List<BlockPos> list2, int k,  IBlockState[] aiblockstate, EnumFacing enumfacing,
                                 int l, BlockPos blockpos3, IBlockState iblockstate2)
    {
        this.blockpos = blockpos3;
    }

    // Only For dev environment
    @Inject(method = "doMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", shift = At.Shift.AFTER, ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void setTileEntityTE(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                                 BlockPistonStructureHelper blockpistonstructurehelper, List<BlockPos> list, List<IBlockState> list1,
                                 List<BlockPos> list2, int k,  IBlockState[] aiblockstate, EnumFacing enumfacing,
                                 int l, BlockPos blockpos3, IBlockState iblockstate2)
    {
        if (!Config.movableTileEntities)
            return;

        this.blockpos = blockpos3;
        setTileEntityTE(worldIn, pos, direction, extending, cir, blockpistonstructurehelper, list, list1, list2, k, aiblockstate, l);
    }
    // Only for dev environment

    @Surrogate
    private void setTileEntityTE(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir,
                          BlockPistonStructureHelper blockpistonstructurehelper, List<BlockPos> list, List<IBlockState> list1,
                          List<BlockPos> list2, int k,  IBlockState[] aiblockstate, int l)
    {
        if (!Config.movableTileEntities)
            return;

        TileEntity e = worldIn.getTileEntity(this.blockpos);
        if (!(e instanceof TileEntityPiston))
            return;

        ((ITileEntityPiston) e).setCarriedBlockEntity(tileEntitiesList.get(l));
    }
    // End MovableTE
    
    @Redirect(method = "checkForMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addBlockEvent(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V", ordinal = 1))
    private void sendDropBlockFlag(World world, BlockPos pos, Block blockIn, int eventID, int eventParam, World worldIn, BlockPos callpos, IBlockState state)
    {
        int suppress_move = 0;
        
        if (Config.pistonGhostBlocksFix.equals("clientAndServer"))
        {
            final EnumFacing enumfacing = state.getValue(FACING);
            
            final BlockPos blockpos = new BlockPos(callpos).offset(enumfacing, 2);
            final IBlockState iblockstate = worldIn.getBlockState(blockpos);
            
            if (iblockstate.getBlock() == Blocks.PISTON_EXTENSION)
            {
                final TileEntity tileentity = worldIn.getTileEntity(blockpos);
                
                if (tileentity instanceof TileEntityPiston)
                {
                    final TileEntityPiston tileentitypiston = (TileEntityPiston) tileentity;
                    if (tileentitypiston.getFacing() == enumfacing && tileentitypiston.isExtending()
                                && (((ITileEntityPiston) tileentitypiston).getLastProgress() < 0.5F
                                            || tileentitypiston.getWorld().getTotalWorldTime() == ((ITileEntityPiston) tileentitypiston).getLastTicked()
                                            || !((IWorldServer) worldIn).haveBlockActionsProcessed()))
                    {
                        suppress_move = 16;
                    }
                }
            }
        }
        
        worldIn.addBlockEvent(pos, blockIn, eventID, eventParam | suppress_move);
    }
    
    @Inject(method = "eventReceived", at = @At("HEAD"))
    private void setEventParam(IBlockState state, World worldIn, BlockPos pos, int id, int param, CallbackInfoReturnable<Integer> cir)
    {
        this.mixinEventParam = param;
    }
    
    @ModifyVariable(method = "eventReceived", name = "flag1", index = 11, at = @At(value = "LOAD", ordinal = 0))
    private boolean didServerDrop(boolean flag1)
    {
        if ((this.mixinEventParam & 16) == 16 && Config.pistonGhostBlocksFix.equals("clientAndServer"))
        {
            flag1 = true;
        }
        
        return flag1;
    }
}
