package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ItemPickaxe.class)
public class MixinsItemPickaxe extends ItemTool {

    protected MixinsItemPickaxe(float attackDamageIn, float attackSpeedIn, ToolMaterial materialIn, Set<Block> effectiveBlocksIn) {
        super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(ItemStack stack, IBlockState state, CallbackInfoReturnable<Float> cir) {
        Material material = state.getMaterial();
        cir.setReturnValue(material != Material.IRON && material != Material.ANVIL && material != Material.ROCK&& (!Config.missingTools || (material != Material.PISTON && material != Material.GLASS)) ? super.getDestroySpeed(stack, state) : this.efficiency);
    }
}
