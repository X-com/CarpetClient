package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.bugfix.PistonFix;
import carpetclient.rules.CarpetRules;
import carpetclient.util.ITileEntityPiston;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.entity.Entity;

import java.util.List;

@Mixin(TileEntityPiston.class)
public class MixinTileEntityPiston extends TileEntity implements ITileEntityPiston
{
    @Shadow private IBlockState pistonState;
    private TileEntity carriedTileEntity;

    public TileEntity getCarriedBlockEntity()
    {
        return carriedTileEntity;
    }

    public void setCarriedBlockEntity(TileEntity tileEntity)
    {
        this.carriedTileEntity = tileEntity;
        if (this.carriedTileEntity != null)
            this.carriedTileEntity.setPos(this.pos);
    }

    /**
     * Updates player being moved to simulate regular game logic where players move before tile entitys.
     *
     * @param p_184322_1_
     * @param ci
     */
    @Inject(method = "moveCollidedEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityPiston;MOVING_ENTITY:Ljava/lang/ThreadLocal;", shift = At.Shift.AFTER, ordinal = 0))
    private void handleRecipeClickedd(float p_184322_1_, CallbackInfo ci) {
        PistonFix.movePlayer();
    }

    /**
     * force updates the player to move the player into the new chunk. Fix for MC-108469.
     *
     * @param p_184322_1_
     * @param ci
     * @param enumfacing
     * @param d0
     * @param list
     * @param axisalignedbb
     * @param list1
     * @param flag
     * @param i
     * @param entity
     * @param d1
     */
    @Inject(method = "moveCollidedEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityPiston;MOVING_ENTITY:Ljava/lang/ThreadLocal;", shift = At.Shift.AFTER, ordinal = 1), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void forceUpdate(float p_184322_1_, CallbackInfo ci, EnumFacing enumfacing, double d0, List<AxisAlignedBB> list, AxisAlignedBB axisalignedbb, List<Entity> list1, boolean flag, int i, Entity entity, double d1) {
        if (!Config.clipThroughPistons.getValue()) return;

        world.updateEntityWithOptionalForce(entity, false);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void readFromNBTTE(NBTTagCompound compound, CallbackInfo ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (compound.hasKey("carriedTileEntity"))
        {
            Block block = this.pistonState.getBlock();
            if (block instanceof ITileEntityProvider)
            {
                this.carriedTileEntity = ((ITileEntityProvider) block).createNewTileEntity(this.world, block.getMetaFromState(this.pistonState));
            }

            if (carriedTileEntity != null)
            {
                this.carriedTileEntity.readFromNBT(compound.getCompoundTag("carriedTileEntity"));
            }
        }
    }

    @Inject(method = "writeToNBT", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void writeToNBTTE(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (carriedTileEntity != null)
        {
            compound.setTag("carriedTileEntity", this.carriedTileEntity.writeToNBT(new NBTTagCompound()));
        }
    }

    @Inject(method = "clearPistonTileEntity", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/tileentity/TileEntityPiston;invalidate()V"), cancellable = true)
    private void clearPistonTileEntityTE(CallbackInfo ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (this.world.getBlockState(this.pos).getBlock() == Blocks.PISTON_EXTENSION)
        {
            this.placeBlock();
        }
        else if (!this.world.isRemote && this.carriedTileEntity != null && this.world.getBlockState(this.pos).getBlock() == Blocks.AIR)
        {
            this.placeBlock();
            this.world.setBlockToAir(this.pos);
        }
        ci.cancel();
    }

    @Inject(method = "update", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/tileentity/TileEntityPiston;invalidate()V"), cancellable = true)
    private void updateTE(CallbackInfo ci)
    {
        if (!Config.movableTileEntities)
            return;

        if (this.world.getBlockState(this.pos).getBlock() == Blocks.PISTON_EXTENSION)
        {
            this.placeBlock();
        }
        ci.cancel();
    }

    private void placeBlock()
    {
        // workaround for the direction change caused by BlockDispenser.onBlockAdded();
        Block block = this.pistonState.getBlock();
        if (block instanceof BlockDispenser || block instanceof BlockFurnace)
        {
            this.world.setBlockState(this.pos, this.pistonState, 18);
        }
        //workaround is just placing the block twice. This should not cause any problems, but is ugly code

        this.world.setBlockState(this.pos, this.pistonState, 18);  //Flag 18 => No block updates, TileEntity has to be placed first

        if (!this.world.isRemote)
        {
            if (carriedTileEntity != null)
            {
                this.world.removeTileEntity(this.pos);
                carriedTileEntity.validate();
                this.world.setTileEntity(this.pos, carriedTileEntity);
            }

            //Update neighbors, comparators and observers now (same order as setBlockState would have if flag was set to 3 (default))
            //This should not change piston behavior for vanilla-pushable blocks at all

            this.world.notifyNeighborsRespectDebug(pos, Blocks.PISTON_EXTENSION, true);
            if (this.pistonState.hasComparatorInputOverride())
            {
                this.world.updateComparatorOutputLevel(pos, this.pistonState.getBlock());
            }
            this.world.updateObservingBlocksAt(pos, this.pistonState.getBlock());
        }
        this.world.neighborChanged(this.pos, this.pistonState.getBlock(), this.pos);
    }
}
