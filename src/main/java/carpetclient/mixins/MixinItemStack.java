package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class to allow empty shulkerboxes being stackable in the player inventory.
 */
@Mixin(ItemStack.class)
public class MixinItemStack {

    @Shadow
    public Item getItem() {
        return null;
    }

    @Shadow
    public boolean hasTagCompound() {
        return false;
    }

    /*
     * Method to allow empty shulkerboxes stack in the players inventory.
     */
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(CallbackInfoReturnable<Integer> cir) {
        if (Config.stackableShulkersPlayerInventory && !hasTagCompound() && this.getItem() instanceof ItemShulkerBox)
            cir.setReturnValue(64);
    }
}
