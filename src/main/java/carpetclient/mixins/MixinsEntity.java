package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
A Mixins class to override a method to change the players aim.
 */
@Mixin(Entity.class)
public abstract class MixinsEntity {

    @Shadow
    public float rotationYaw;
    @Shadow
    private Entity ridingEntity;

    @Shadow public World world;
    private float storedRotationYaw;

//    /*
//    Override to change the behavior of player aiming.
//     */
//    @Overwrite
//    public void turn(float yaw, float pitch) {
//        float f = this.rotationPitch;
//        float f1 = this.rotationYaw;
//        this.rotationYaw = (float) ((double) this.rotationYaw + (double) yaw * 0.15D);
//        this.rotationPitch = (float) ((double) this.rotationPitch - (double) pitch * 0.15D);
//        this.rotationPitch = MathHelper.clamp(this.rotationPitch, -90.0F, 90.0F);
//        this.prevRotationPitch += this.rotationPitch - f;
//        
//        snapAim(this.ridingEntity != null, yaw);
//        this.prevRotationYaw += this.rotationYaw - f1;
//
//        if (this.ridingEntity != null) {
//            this.ridingEntity.applyOrientationToEntity((Entity) (Object) this);
//        }
//    }

    /*
    Injection to modify the behavior of player aiming.
     */
    @Inject(method = "turn", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F", opcode = Opcodes.PUTFIELD))
    public void post(float yaw, float pitch, CallbackInfo ci) {
        snapAim(this.ridingEntity, yaw);
    }

    /**
     * Updates the value stored rotation yaw so no jurking actions is done when turning on the rotation.
     */
    public void updateStoredRotationYaw(){
        storedRotationYaw = rotationYaw;
    }
    
    /**
     * Overriding the aim of the player to snap to angles of 45 degrees
     * @param riding Gets the type of entity the player is riding.
     * @param yaw The angle in which the player will turn, sent in from the turn method.
     */
    private void snapAim(Entity riding, float yaw) {
        boolean inBoat = false;
        
        if(riding != null && riding instanceof EntityBoat){
            inBoat = true;
        }
        
        if (inBoat || !Config.snapAim){
            updateStoredRotationYaw();
            return;
        }

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

    @Redirect(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/MoverType;PISTON:Lnet/minecraft/entity/MoverType;", opcode = Opcodes.GETSTATIC))
    public MoverType redirectMoveType() {
        if(world.isRemote){
            return null;
        }
        return MoverType.PISTON;
    }
}
