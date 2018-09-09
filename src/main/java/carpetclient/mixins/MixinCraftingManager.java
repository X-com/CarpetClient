package carpetclient.mixins;

import carpetclient.NewCrafting;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Class to add recipes such as rockets to the crafting recipe book when server sends the recipes unlock.
 */
@Mixin(CraftingManager.class)
public class MixinCraftingManager {

    private static IRecipe rocketOne;
    private static IRecipe rocketTwo;
    private static IRecipe rocketThree;

    @Shadow
    private static boolean parseJsonRecipes() {
        return true;
    }

    @Shadow
    public static void register(String p_register_0_, IRecipe p_register_1_) {
    }

    /**
     * Injection to the init method to register the rocket recipes before the any other recipes are added. Critical to not add them before other recipes as there is a bug with rockets if added after the Firework recipes are added.
     */
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;register(Ljava/lang/String;Lnet/minecraft/item/crafting/IRecipe;)V", ordinal = 10, shift = At.Shift.AFTER))
    private static void inectInit(CallbackInfoReturnable<Boolean> cir) {
        try {
            boolean result = parseJsonRecipes();
            NewCrafting.createRockets();
            register("rocket1", NewCrafting.rocketOne);
            register("rocket2", NewCrafting.rocketTwo);
            register("rocket3", NewCrafting.rocketThree);
            cir.setReturnValue(result);
        } catch (Throwable var1) {
            var1.printStackTrace();
            cir.setReturnValue(false);
        }
    }
}
