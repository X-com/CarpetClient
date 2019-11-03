package carpetclient.mixins;

import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookClient.class)
public interface IMixinRecipeBookClient {

    @Invoker
    static RecipeList callNewRecipeList(CreativeTabs srcTab) {
        return null;
    }

    @Invoker
    static CreativeTabs callGetItemStackTab(ItemStack stackIn) {
        return null;
    }
}
