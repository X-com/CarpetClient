package carpetclient.mixins;

import carpetclient.Config;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mixin(World.class)
public class MixinWorld {

    @Shadow @Final public List<TileEntity> loadedTileEntityList;
    @Shadow @Final public List<TileEntity> tickableTileEntities;
    @Shadow @Final private List<TileEntity> tileEntitiesToBeRemoved;

    /**
     * Ignoring entity's when placing  block clientside.
     * @param world
     * @param bb
     * @param entityIn
     * @param blockIn
     * @param pos
     * @param skipCollisionCheck
     * @param sidePlacedOn
     * @param placer
     * @return
     */
    @Redirect(method = "mayPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;checkNoEntityCollision(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Z"))
    private boolean ignoreEntitysWhenPlacingBlocks(World world,
                                                    AxisAlignedBB bb, @Nullable Entity entityIn, // sub vars
                                                    Block blockIn, BlockPos pos, boolean skipCollisionCheck, EnumFacing sidePlacedOn, @Nullable Entity placer// main vars
    ){
        return Config.ignoreEntityWhenPlacing || world.checkNoEntityCollision(bb, entityIn);
    }

    /**
     * Tile entity removal lag fix from forge.
     *
     * @param ci
     */
    @Inject(method = "updateEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tileEntitiesToBeRemoved:Ljava/util/List;", ordinal = 0))
    private void tileEntityRemovalFix(CallbackInfo ci){
        if (!this.tileEntitiesToBeRemoved.isEmpty()) {
            Set<TileEntity> remove = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(this.tileEntitiesToBeRemoved);
            this.tickableTileEntities.removeAll(remove);
            this.loadedTileEntityList.removeAll(remove);
            this.tileEntitiesToBeRemoved.clear();
        }
    }

    /**
     * Prevent updateEntities targeting the player during world tick
     *
     * @param world
     * @param ent
     */
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"))
    private void noPlayerUpdateDuringWorld(World world, Entity ent){
        if (!(ent instanceof EntityPlayerSP))
            world.updateEntity(ent);
    }
}
