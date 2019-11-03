package carpetclient.mixins;

import carpetclient.mixinInterface.AMixinRegistryNamespaced;
import net.minecraft.util.IObjectIntIterable;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.registry.RegistrySimple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(RegistryNamespaced.class)
public class MixinRegistryNamespaced<K, V> extends RegistrySimple<K, V> implements IObjectIntIterable<V>, AMixinRegistryNamespaced {

    @Shadow
    @Final
    protected IntIdentityHashBiMap<V> underlyingIntegerMap = new IntIdentityHashBiMap<V>(256);

    @Shadow
    @Final
    protected Map<V, K> inverseObjectRegistry;

    public void clear() {
        underlyingIntegerMap.clear();
        inverseObjectRegistry.clear();
        registryObjects.clear();
    }
}
