package carpetclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import carpetclient.NewCrafting;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.crafting.CraftingManager;

@Mixin(Bootstrap.class)
public class MixinBootstrap {

    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/CraftingManager;init()Z", shift = At.Shift.AFTER))
    private static void addExtraCraftingRecipes(CallbackInfo ci) {
        NewCrafting.createRockets();
        CraftingManager.register("rocket1", NewCrafting.rocketOne);
        CraftingManager.register("rocket2", NewCrafting.rocketTwo);
        CraftingManager.register("rocket3", NewCrafting.rocketThree);
    }
    
}
