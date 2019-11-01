package carpetclient.mixins;

import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RegistryNamespaced.class)
public interface IMixinRegistryNamespaced<K, V> {
    @Accessor("underlyingIntegerMap")
    IntIdentityHashBiMap<V> getUnderlyingIntegerMap();

    @Accessor("inverseObjectRegistry")
    Map<V, K> getInverseObjectRegistry();
}
