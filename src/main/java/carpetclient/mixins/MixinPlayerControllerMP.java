package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.Hotkeys;
import carpetclient.coders.skyrising.PacketSplitter;
import com.mumfrey.liteloader.core.PluginChannels;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
Injecting code for block rotation. Editing the x value when sending the package "CPacketPlayerTryUseItemOnBlock" to be decoded by carpet.
 */

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Shadow
    private Minecraft mc;
    @Shadow
    private GameType currentGameType;
    @Shadow
    private @Final
    NetHandlerPlayClient connection;

    @Shadow
    private void syncCurrentPlayItem() {
    }

    /**
     * Totally crazy problem solved by redirect. omg!
     * Changed this value to use accurate block placement to rotate blocks.
     *
     * @param connection
     * @param packetIn
     * @param player
     * @param worldIn
     * @param pos
     * @param direction
     * @param vec
     * @param hand
     */
    @Redirect(method = "processRightClickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void sendPacketReplace(NetHandlerPlayClient connection,
                                  Packet<?> packetIn, // sendPacket vars
                                  EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand // processRightClickBlock vars
    ) {
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());
        ItemStack item = player.getHeldItem(hand);
        if (Config.accurateBlockPlacement) f = blockRotation(player, pos, f, direction, item);
        connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
    }

    /**
     * Checks for the item types that should be accurate placed, skipps everything else. if f value is above 1 then the protocal is already being used and also returns false to skip rotation.
     */
    private boolean rotationType(float f, ItemStack is) {
        if (f > 1) {
            return false;
        }

        if (isPiston(is)) {
            return true;
        } else if (isObserver(is)) {
            return true;
        } else if (isDiode(is)) {
            return true;
        }else if(isDispenser(is)){
            return true;
        }else if(isGlazedTerracotta(is)){
            return true;
        }

        return false;
    }


//    @ModifyVariable(method = "processRightClickBlock", ordinal = 0, at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;connection:Lnet/minecraft/client/network/NetHandlerPlayClient;"))
    /**
     * A rotation alogrithm that will sneek data in the unused x value. Data will be decoded by carpet
     * mod "accurateBlockPlacement" and place the block in the orientation that is coded.
     *
     * @param player    The player that is plasing the block.
     * @param pos       Position of the Block being placed
     * @param f         old X value that is used unused currently in vanilla minecraft. Y value is used to place blocks on the top of bottom part (stairs/slabs).
     * @param direction The direction of the block being placed into. Rather the facing side the player is clicking on.
     * @param itemstack The item stack or item that is being placed.
     * @return value that is coded for specific orientation that is determined by the players choices.
     */
    private float blockRotation(EntityPlayerSP player, BlockPos pos, float f, EnumFacing direction, ItemStack itemstack) {
        if (!rotationType(f, itemstack)) return f;

        if (Hotkeys.isKeyDown(Hotkeys.toggleBlockFacing.getKeyCode())) {
            // rotate pistons for placing head into blocks
            if (isPiston(itemstack)) {
                direction = direction.getOpposite();
            }

            float i = Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode()) ? direction.getOpposite().getIndex() : direction.getIndex();
            return i + 2;
        } else {
            EnumFacing facing = null;

            // different type of placement for diodes
            if (isDiode(itemstack) || isGlazedTerracotta(itemstack)) {
                facing = player.getHorizontalFacing().getOpposite();
            } else {
                facing = EnumFacing.getDirectionFromEntityLiving(pos.offset(direction), player);
            }

            // rotate for observers
            if (isObserver(itemstack)) {
                facing = facing.getOpposite();
            }

//            float i = GuiScreen.isAltKeyDown() ? facing.getOpposite().getIndex() : facing.getIndex();
            float i = Hotkeys.isKeyDown(Hotkeys.toggleBlockFlip.getKeyCode()) ? facing.getOpposite().getIndex() : facing.getIndex();
            return i + 2;
        }
    }

    /**
     * Checks to see if item is a repeater/comperator that is being placed.
     *
     * @param itemstack The stack that is being placed.
     * @return true if item is repeater/comperator that is being placed or not.
     */
    private boolean isDiode(ItemStack itemstack) {
        int id = Item.getIdFromItem(itemstack.getItem());
        return id == 356 || id == 404;
    }

    /**
     * Checks to see if item is a piston/sticky-piston that is being placed.
     *
     * @param itemstack The stack that is being placed.
     * @return true if item is piston/sticky-piston that is being placed or not.
     */
    private boolean isPiston(ItemStack itemstack) {
        int id = Item.getIdFromItem(itemstack.getItem());
        return id == 29 || id == 33;
    }

    /**
     * Checks to see if item is a observer that is being placed.
     *
     * @param itemstack The stack that is being placed.
     * @return true if item is observer that is being placed or not.
     */
    public boolean isObserver(ItemStack itemstack) {
        int id = Item.getIdFromItem(itemstack.getItem());
        return id == 218;
    }

    /**
     * Checks to see if its dispenser or dropper.
     * 
     * @param itemstack The item that is to be checked if it should be allowed to get player rotated.
     * @return Returns if the item type is allowed or not to be rotated.
     */
    private boolean isDispenser(ItemStack itemstack) {
        int id = Item.getIdFromItem(itemstack.getItem());
        return id == 23 || id == 158;
    }

    /**
     * Checks to see if its any kind of glazed terracotta.
     *
     * @param itemstack The item that is to be checked if it should be allowed to get player rotated.
     * @return Returns if the item type is allowed or not to be rotated.
     */
    private boolean isGlazedTerracotta(ItemStack itemstack){
        int id = Item.getIdFromItem(itemstack.getItem());
        return id >= 235 && id <= 250;
    }

    @Inject(method = "func_194338_a", at = @At("RETURN"))
    private void onSendPlaceRecipe(int window, IRecipe recipe, boolean makeAll, EntityPlayer p_194338_4_, CallbackInfo ci) {
        if(!Config.fastCrafting.getValue()) return;
        if (GuiScreen.isShiftKeyDown() && GuiScreen.isAltKeyDown() ){
            this.mc.playerController.windowClick(window, 0, 1, ClickType.QUICK_MOVE, this.mc.player);
        } else if(GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown() && Config.controlQCrafting) {
            this.mc.playerController.windowClick(window, 0, 1, ClickType.THROW, this.mc.player);
        }
    }

    // CrispyLumps was here

    /**
     * Fixes the mining packets for carpet client users to add careful break and remove blocks shortly reappearing when mining slower then instant mine.
     */
    @Redirect(method = "clickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 2))
    public void miningPacket(NetHandlerPlayClient connection,
                                  Packet<?> packetIn, // sendPacket vars
                                    BlockPos loc, EnumFacing face // processRightClickBlock vars
    ) {
        if(!Config.betterMiner) {
            connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
            return;
        }
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        IBlockState iblockstate = this.mc.world.getBlockState(loc);
        boolean instaMine = iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, loc) >= 1.0F;
        data.writeBoolean(true);
        data.writeBlockPos(loc);
        data.writeByte(face.getIndex());
        data.writeBoolean(instaMine);
        data.writeBoolean(Config.carefulBreak.getValue());

        PacketSplitter.send("carpet:mine", data, PluginChannels.ChannelPolicy.DISPATCH_ALWAYS);
    }

    /**
     * Fixes the mining packets for carpet client users to add careful break and remove blocks shortly reappearing when mining slower then instant mine.
     */
    @Redirect(method = "onPlayerDamageBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 1))
    public void miningPacketEnd(NetHandlerPlayClient connection,
                             Packet<?> packetIn, // sendPacket vars
                             BlockPos loc, EnumFacing face // processRightClickBlock vars
    ) {
        if(!Config.betterMiner) {
            connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, loc, face));
            return;
        }

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeBoolean(false);
        data.writeBlockPos(loc);
        data.writeByte(face.getIndex());
        data.writeBoolean(true);
        data.writeBoolean(Config.carefulBreak.getValue());

        PacketSplitter.send("carpet:mine", data, PluginChannels.ChannelPolicy.DISPATCH_ALWAYS);
    }

}
