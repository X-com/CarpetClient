package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.bugfix.PistonFix;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.entity.Entity;

import java.util.List;

@Mixin(TileEntityPiston.class)
public class MixinTileEntityPiston extends TileEntity {

    /**
     * Updates player being moved to simulate regular game logic where players move before tile entitys.
     *
     * @param p_184322_1_
     * @param ci
     */
    @Inject(method = "moveCollidedEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityPiston;MOVING_ENTITY:Ljava/lang/ThreadLocal;", shift = At.Shift.AFTER, ordinal = 0))
    private void handleRecipeClickedd(float p_184322_1_, CallbackInfo ci) {
        PistonFix.movePlayer();
    }

    /**
     * force updates the player to move the player into the new chunk. Fix for MC-108469.
     *
     * @param p_184322_1_
     * @param ci
     * @param enumfacing
     * @param d0
     * @param list
     * @param axisalignedbb
     * @param list1
     * @param flag
     * @param i
     * @param entity
     * @param d1
     */
    @Inject(method = "moveCollidedEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityPiston;MOVING_ENTITY:Ljava/lang/ThreadLocal;", shift = At.Shift.AFTER, ordinal = 1), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void forceUpdate(float p_184322_1_, CallbackInfo ci, EnumFacing enumfacing, double d0, List<AxisAlignedBB> list, AxisAlignedBB axisalignedbb, List<Entity> list1, boolean flag, int i, Entity entity, double d1) {
        if (!Config.clipThroughPistons.getValue()) return;

        world.updateEntityWithOptionalForce(entity, false);
    }
}
