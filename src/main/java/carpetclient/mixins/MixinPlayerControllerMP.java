package carpetclient.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
Injecting code for block rotation. Editing the x value when sending the package "CPacketPlayerTryUseItemOnBlock" to be decoded by carpet.
 */

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    private static final String TARGET = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/Packet;)V";
    @Shadow private Minecraft mc;
    @Shadow private GameType currentGameType;
    @Shadow private @Final NetHandlerPlayClient connection;
    @Shadow private void syncCurrentPlayItem() { }

    // Nasty override cause Injection dosen't work. Hopefully this will be replaced by a clean Injection
    @Overwrite
    public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand) {
        this.syncCurrentPlayItem();
        ItemStack itemstack = player.getHeldItem(hand);
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());
        boolean flag = false;

        if (!this.mc.world.getWorldBorder().contains(pos)) {
            return EnumActionResult.FAIL;
        } else {
            if (this.currentGameType != GameType.SPECTATOR) {
                IBlockState iblockstate = worldIn.getBlockState(pos);

                if ((!player.isSneaking() || player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) && iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, direction, f, f1, f2)) {
                    flag = true;
                }

                if (!flag && itemstack.getItem() instanceof ItemBlock) {
                    ItemBlock itemblock = (ItemBlock) itemstack.getItem();

                    if (!itemblock.canPlaceBlockOnSide(worldIn, pos, direction, player, itemstack)) {
                        return EnumActionResult.FAIL;
                    }
                }
            }

            f = blockRotation(player, pos, f, direction, itemstack);
            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));

            if (!flag && this.currentGameType != GameType.SPECTATOR) {
                if (itemstack.isEmpty()) {
                    return EnumActionResult.PASS;
                } else if (player.getCooldownTracker().hasCooldown(itemstack.getItem())) {
                    return EnumActionResult.PASS;
                } else {
                    if (itemstack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
                        Block block = ((ItemBlock) itemstack.getItem()).getBlock();

                        if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                            return EnumActionResult.FAIL;
                        }
                    }

                    if (this.currentGameType.isCreative()) {
                        int i = itemstack.getMetadata();
                        int j = itemstack.getCount();
                        EnumActionResult enumactionresult = itemstack.onItemUse(player, worldIn, pos, hand, direction, f, f1, f2);
                        itemstack.setItemDamage(i);
                        itemstack.setCount(j);
                        return enumactionresult;
                    } else {
                        return itemstack.onItemUse(player, worldIn, pos, hand, direction, f, f1, f2);
                    }
                }
            } else {
                return EnumActionResult.SUCCESS;
            }
        }
    }

    /**
     * A rotation alogrithm that will sneek data in the unused x value. Data will be decoded by carpet 
     * mod "accurateBlockPlacement" and place the block in the orientation that is coded.
     * @param player The player that is plasing the block.
     * @param pos Position of the Block being placed
     * @param f old X value that is used unused currently in vanilla minecraft. Y value is used to place blocks on the top of bottom part (stairs/slabs).
     * @param direction The direction of the block being placed into. Rather the facing side the player is clicking on.
     * @param itemstack The item stack or item that is being placed.
     * @return value that is coded for specific orientation that is determined by the players choices.
     */
    private float blockRotation(EntityPlayerSP player, BlockPos pos, float f, EnumFacing direction, ItemStack itemstack) {
        if(GuiScreen.isCtrlKeyDown()){
            
            // rotate pistons for placing head into blocks
            if(isPiston(itemstack)){
                direction = direction.getOpposite();
            }
            
            float i = GuiScreen.isAltKeyDown() ? direction.getOpposite().getIndex() : direction.getIndex();
            return i + 2;
        }else{
            EnumFacing facing = null;
            
            // different type of placement for diodes
            if(isDiode(itemstack) ){
                facing = player.getHorizontalFacing().getOpposite();
            }else{
                facing = EnumFacing.getDirectionFromEntityLiving(pos.offset(direction), player);
            }
            
            // rotate for observers
            if(isObserver(itemstack)){
                facing = facing.getOpposite();
            }
            
            float i = GuiScreen.isAltKeyDown() ? facing.getOpposite().getIndex() : facing.getIndex();
            return i + 2;
        }
    }

    /**
     * Checks to see if item is a repeater/comperator that is being placed.
     * @param itemstack The stack that is being placed.
     * @return true if item is repeater/comperator that is being placed or not.
     */
    private boolean isDiode(ItemStack itemstack) {
        int id = Item.getIdFromItem(itemstack.getItem());
        return id == 356 || id == 404;
    }

    /**
     * Checks to see if item is a piston/sticky-piston that is being placed.
     * @param itemstack The stack that is being placed.
     * @return true if item is piston/sticky-piston that is being placed or not.
     */
    private boolean isPiston(ItemStack itemstack){
        int id = Item.getIdFromItem(itemstack.getItem());
        return id == 29 || id == 33;
    }

    /**
     * Checks to see if item is a observer that is being placed.
     * @param itemstack The stack that is being placed.
     * @return true if item is observer that is being placed or not.
     */
    public boolean isObserver(ItemStack itemstack) {
        int id = Item.getIdFromItem(itemstack.getItem());
        return id == 218;
    }
}
