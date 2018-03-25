package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ItemShears.class)
public class MixinsItemShears extends Item {

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    public void canPlaceOnOver(ItemStack stack, IBlockState state, CallbackInfoReturnable<Float> cir) {
        Block block = state.getBlock();

        if (block != Blocks.WEB && state.getMaterial() != Material.LEAVES && (!Config.missingTools || state.getMaterial() != Material.SPONGE)) {
            cir.setReturnValue(block == Blocks.WOOL ? 5.0F : super.getDestroySpeed(stack, state));
        } else {
            cir.setReturnValue(15.0F);
        }
    }
}
