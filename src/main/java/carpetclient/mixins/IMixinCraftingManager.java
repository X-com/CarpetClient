package carpetclient.mixins;

import net.minecraft.item.crafting.CraftingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingManager.class)
public interface IMixinCraftingManager {

    @Accessor("nextAvailableId")
    static void setNextAvailableId(int i) {
        throw new AssertionError("Mixin didn't patch me!");
    }
}
