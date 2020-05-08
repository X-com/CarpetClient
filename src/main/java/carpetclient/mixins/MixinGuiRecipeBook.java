package carpetclient.mixins;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.inventory.InventoryCrafting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiRecipeBook.class)
public class MixinGuiRecipeBook {

    @Shadow  private GuiTextField searchBar;

    @Shadow  private String lastSearch;

    private static String memoText = "";

    @Inject(method = "initVisuals", at = @At("TAIL"))
    private void setSearchText(boolean p_193014_1_, InventoryCrafting p_193014_2_, CallbackInfo c) {
        searchBar.setText(memoText);
    }

    @Inject(method = "keyPressed", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/recipebook/GuiRecipeBook;lastSearch:Ljava/lang/String;", ordinal = 1, shift = At.Shift.AFTER))
    private void setSearchText(char typedChar, int keycode, CallbackInfoReturnable<Boolean> c) {
        memoText = lastSearch;
    }
}
