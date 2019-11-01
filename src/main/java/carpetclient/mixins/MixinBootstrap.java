package carpetclient.mixins;

import java.io.IOException;
import java.lang.reflect.Method;

import carpetclient.util.CustomCrafting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import carpetclient.NewCrafting;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

@Mixin(Bootstrap.class)
public class MixinBootstrap {

    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;init()Z", shift = At.Shift.AFTER))
    private static void addExtraCraftingRecipes(CallbackInfo ci) throws IOException {
//        CustomCrafting.createRockets();
//        registerRecipe("rocket1", CustomCrafting.rocketOne);
//        registerRecipe("rocket2", CustomCrafting.rocketTwo);
//        registerRecipe("rocket3", CustomCrafting.rocketThree);
//        CustomCrafting.registerCustomRecipes();

//        for (ResourceLocation irecipe : CraftingManager.REGISTRY.getKeys())
//        {
//            System.out.println(irecipe);
//        }
    }

    private static void registerRecipe(String name, IRecipe recipe) {
        try {
            CraftingManager.register(name, recipe);
        } catch (IllegalAccessError e) {
            // Method is private, Forge is present
            try {
                Method method = CraftingManager.class.getDeclaredMethod("func_193379_a", String.class, IRecipe.class);
                method.setAccessible(true);
                method.invoke(null, name, recipe);
            } catch (Exception e1) {
                LogManager.getLogger().error("Couldn't add recipe", e1);
            }
        }
    }
}
