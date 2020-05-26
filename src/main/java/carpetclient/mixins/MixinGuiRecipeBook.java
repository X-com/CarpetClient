package carpetclient.mixins;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow public boolean isVisible(){return false;}

    @Shadow @Final private RecipeBookPage recipeBookPage;

    @Shadow private int width;

    @Shadow private int height;

    @Shadow private int xOffset;

    @Shadow private void updateCollections(boolean p_193003_1_) {}

    private static String memoText = "";

    @Inject(method = "initVisuals", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/recipebook/GuiRecipeBook;searchBar:Lnet/minecraft/client/gui/GuiTextField;", ordinal = 4, shift = At.Shift.AFTER))
    private void setSearchText(boolean p_193014_1_, InventoryCrafting p_193014_2_, CallbackInfo c) {
        if(!isVisible()){
            memoText = "";
        }
        searchBar.setText(memoText);
    }

    @Inject(method = "keyPressed", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/recipebook/GuiRecipeBook;lastSearch:Ljava/lang/String;", ordinal = 1, shift = At.Shift.AFTER))
    private void setSearchText(char typedChar, int keycode, CallbackInfoReturnable<Boolean> c) {
        memoText = lastSearch;
    }

    @Inject(method = "mouseClicked", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/recipebook/GuiRecipeBook;recipeBookPage:Lnet/minecraft/client/gui/recipebook/RecipeBookPage;", ordinal = 2, shift = At.Shift.AFTER))
    private void setSearchText(int p_191862_1_, int p_191862_2_, int p_191862_3_, CallbackInfoReturnable<Boolean> c) {
        if(p_191862_3_ == 2) {
            try {
                recipeBookPage.mouseClicked(p_191862_1_, p_191862_2_, 0, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166);
                IRecipe irecipe = recipeBookPage.getLastClickedRecipe();
                String s = irecipe.getRecipeOutput().getDisplayName();
                searchBar.setText(s);
                lastSearch = s;
                memoText = s;
                this.updateCollections(true);
            }catch (Exception e){}
        }
    }
}
