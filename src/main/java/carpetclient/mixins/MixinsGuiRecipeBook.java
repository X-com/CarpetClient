package carpetclient.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeUpdateListener;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.crafting.IRecipe;

@Mixin(GuiRecipeBook.class)
public abstract class MixinsGuiRecipeBook extends Gui implements IRecipeUpdateListener {
    @Shadow
    private Minecraft mc;
    
    @Inject(method = "handleRecipeClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;markDirty()V", shift = At.Shift.AFTER))
    private void handleRecipeClickedd(IRecipe p_193950_1_, List<Slot> p_193950_2_, int p_193950_3_, InventoryCraftResult p_193950_4_, CallbackInfo ci) {
        if (GuiScreen.isShiftKeyDown() && GuiScreen.isAltKeyDown() ){
            this.mc.playerController.windowClick(p_193950_3_, 0, 1, ClickType.QUICK_MOVE, this.mc.player);
        } else if(GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown()) {
            this.mc.playerController.windowClick(p_193950_3_, 0, 1, ClickType.THROW, this.mc.player);
        }
    }
}