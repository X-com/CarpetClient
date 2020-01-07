package carpetclient.mixins;

import carpetclient.mixinInterface.AMixinSearchTree;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.List;
import net.minecraft.client.util.SearchTree;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Search tree cleaning, for custom crafting
 */
@Mixin(SearchTree.class)
public abstract class MixinSearchTree implements AMixinSearchTree {
    @Shadow
    private @Final List<?> contents;

    @Shadow
    private Object2IntMap<?> numericContents;

    @Override
    public void clear() {
        this.numericContents.clear();
        this.contents.clear();
    }
}
