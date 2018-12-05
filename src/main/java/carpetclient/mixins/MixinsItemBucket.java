package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ItemBucket.class)
public class MixinsItemBucket extends Item {

    @Shadow
    private @Final
    Block containedBlock;

    @Shadow
    private ItemStack fillBucket(ItemStack emptyBuckets, EntityPlayer player, Item fullBucket) {
        return null;
    }

    @Shadow
    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn) {
        return false;
    }

    /*
     * Overrides to remove clientside placement or removal of world liquids to force the server to only do it fixing liquid ghost blocks.
     */
    @Overwrite
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        boolean flag = this.containedBlock == Blocks.AIR;
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, flag);

        if (raytraceresult == null) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        } else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            BlockPos blockpos = raytraceresult.getBlockPos();

            if (!worldIn.isBlockModifiable(playerIn, blockpos)) {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
            } else if (flag) {
                if (!playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                } else {
                    IBlockState iblockstate = worldIn.getBlockState(blockpos);
                    Material material = iblockstate.getMaterial();

                    if (material == Material.WATER && ((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
                        if (!worldIn.isRemote || !Config.bucketGhostBlockFix) {
                            worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                            playerIn.addStat(StatList.getObjectUseStats(this));
                            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillBucket(itemstack, playerIn, Items.WATER_BUCKET));
                        }
                        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
                    } else if (material == Material.LAVA && ((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
                        playerIn.playSound(SoundEvents.ITEM_BUCKET_FILL_LAVA, 1.0F, 1.0F);
                        if (!worldIn.isRemote || !Config.bucketGhostBlockFix) {
                            worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 11);
                            playerIn.addStat(StatList.getObjectUseStats(this));
                            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillBucket(itemstack, playerIn, Items.LAVA_BUCKET));
                        }
                        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
                    } else {
                        return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                    }
                }
            } else {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
                BlockPos blockpos1 = flag1 && raytraceresult.sideHit == EnumFacing.UP ? blockpos : blockpos.offset(raytraceresult.sideHit);

                if (!playerIn.canPlayerEdit(blockpos1, raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                } else if (this.tryPlaceContainedLiquid(playerIn, worldIn, blockpos1)) {
                    if (!worldIn.isRemote) {
                        if (playerIn instanceof EntityPlayerMP) {
                            CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) playerIn, blockpos1, itemstack);
                        }

                        playerIn.addStat(StatList.getObjectUseStats(this));
                        return !playerIn.capabilities.isCreativeMode ? new ActionResult(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(EnumActionResult.SUCCESS, itemstack);
                    }
                    return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
                } else {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                }
            }
        }
    }

    /*
     * Edit the play sound and return to remove clientside liquid updates.
     */
    @Inject(method = "tryPlaceContainedLiquid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"), cancellable = true)
    public void tryPlaceContainedLiquidInject(EntityPlayer player, World worldIn, BlockPos posIn, CallbackInfoReturnable<Boolean> cir) {
        if(!Config.bucketGhostBlockFix) return;

        if(this.containedBlock != null && player != null) {
            SoundEvent soundevent = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
            player.playSound(soundevent, 1.0F, 1.0F);
            if (worldIn.isRemote) cir.setReturnValue(true);
        }
    }

//    @Overwrite
//    public boolean tryPlaceContainedLiquid(@Nullable EntityPlayer player, World worldIn, BlockPos posIn) {
//        if (this.containedBlock == Blocks.AIR) {
//            return false;
//        } else {
//            IBlockState iblockstate = worldIn.getBlockState(posIn);
//            Material material = iblockstate.getMaterial();
//            boolean flag = !material.isSolid();
//            boolean flag1 = iblockstate.getBlock().isReplaceable(worldIn, posIn);
//
//            if (!worldIn.isAirBlock(posIn) && !flag && !flag1) {
//                return false;
//            } else {
//                if (worldIn.provider.doesWaterVaporize() && this.containedBlock == Blocks.FLOWING_WATER) {
//                    int l = posIn.getX();
//                    int i = posIn.getY();
//                    int j = posIn.getZ();
//                    worldIn.playSound(player, posIn, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
//
//                    for (int k = 0; k < 8; ++k) {
//                        worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double) l + Math.random(), (double) i + Math.random(), (double) j + Math.random(), 0.0D, 0.0D, 0.0D);
//                    }
//                } else {
//                    if (!worldIn.isRemote && (flag || flag1) && !material.isLiquid()) {
//                        worldIn.destroyBlock(posIn, true);
//                    }
//
//                    SoundEvent soundevent = this.containedBlock == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
////                    worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
//                    if (!player.equals(Minecraft.getMinecraft().player)) {
//                        worldIn.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
//                    } else {
//                        player.playSound(soundevent, 1.0F, 1.0F);
//                    }
//                    if (!worldIn.isRemote) {
//                        System.out.println("containedBlock " + containedBlock);
//                        worldIn.setBlockState(posIn, this.containedBlock.getDefaultState(), 11);
//                    }
//                }
//
//                return true;
//            }
//        }
//    }
}
