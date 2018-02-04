package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

/*
A Mixins class to override a method to change the players aim.
 */
@Mixin(Entity.class)
public abstract class MixinsEntity {

    @Shadow
    public float rotationYaw;
    @Shadow
    public float rotationPitch;
    @Shadow
    public float prevRotationPitch;
    @Shadow
    public float prevRotationYaw;
    @Shadow
    private Entity ridingEntity;

    private float storedRotationYaw;

    /*
    Override to change the behavior of player aiming.
     */
    @Overwrite
    public void turn(float yaw, float pitch) {
        float f = this.rotationPitch;
        float f1 = this.rotationYaw;
        this.rotationYaw = (float) ((double) this.rotationYaw + (double) yaw * 0.15D);
        this.rotationPitch = (float) ((double) this.rotationPitch - (double) pitch * 0.15D);
        this.rotationPitch = MathHelper.clamp(this.rotationPitch, -90.0F, 90.0F);
        this.prevRotationPitch += this.rotationPitch - f;
        
        snapAim(this.ridingEntity != null, yaw);
        this.prevRotationYaw += this.rotationYaw - f1;

        if (this.ridingEntity != null) {
            this.ridingEntity.applyOrientationToEntity((Entity) (Object) this);
        }
    }

    /**
     * Overriding the aim of the player to snap to angles of 45 degrees
     * @param inBoat Checks if the player is riding a boat to skip snapping.
     * @param yaw The angle in which the player will turn, sent in from the turn method.
     */
    private void snapAim(boolean inBoat, float yaw) {
        if (inBoat || !Config.snapAim) return;

        this.storedRotationYaw = (float) ((double) this.storedRotationYaw + (double) yaw * 0.15D);

        float r = storedRotationYaw - storedRotationYaw % 45;
        float r2 = Math.abs(r - storedRotationYaw);
        if (r2 < 5) {
            this.rotationYaw = r;
        } else if (r2 > 40) {
            if (storedRotationYaw < 0)
                this.rotationYaw = r - 45;
            else
                this.rotationYaw = r + 45;
        } else {
            this.rotationYaw = storedRotationYaw;
        }
    }
}
