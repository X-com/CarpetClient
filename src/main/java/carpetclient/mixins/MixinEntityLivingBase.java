package carpetclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import carpetclient.Config;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

@Mixin(value = EntityLivingBase.class, priority = 999)
public abstract class MixinEntityLivingBase extends Entity {

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z", ordinal = 1))
    public boolean redirectOnElytraLanded(World world) {
        if (Config.elytraFix)
            return world.isRemote && !((Entity) this instanceof EntityPlayerSP);
        else
            return world.isRemote;
    }
}
