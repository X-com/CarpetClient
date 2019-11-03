package carpetclient.mixins;

import net.minecraft.client.util.RecipeBookClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeBookClient.class)
public class MixinRecipeBookClient {
    
    public static reset(){
        
    }
}
