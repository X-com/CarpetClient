package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.rules.TickRate;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Tick rate editing in EntityRenderer.java based on Cubitecks tick rate mod.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    private @Final
    Minecraft mc;
    @Shadow
    private float farPlaneDistance;
    @Shadow
    private int frameCount;
    @Shadow
    private boolean debugView;
    @Shadow
    private boolean renderHand;
    @Shadow
    public static int anaglyphField;

    @Shadow
    private boolean isDrawBlockOutline() {
        return true;
    }

    @Shadow
    private void updateFogColor(float partialTicks) {
    }

    @Shadow
    private void setupCameraTransform(float partialTicks, int pass) {
    }

    @Shadow
    private void setupFog(int startCoords, float partialTicks) {
    }

    @Shadow
    private float getFOVModifier(float partialTicks, boolean useFOVSetting) {
        return 0.0f;
    }

    @Shadow
    private void renderCloudsCheck(RenderGlobal renderGlobalIn, float partialTicks, int pass, double x, double y, double z) {
    }

    @Shadow
    public void disableLightmap() {
    }

    @Shadow
    public void enableLightmap() {
    }

    @Shadow
    protected void renderRainSnow(float partialTicks) {
    }

    @Shadow
    private void renderHand(float partialTicks, int pass) {
    }

    @Shadow
    private void updateLightmap(float partialTicks) {
    }

    @Shadow
    public void getMouseOver(float partialTicks) {
    }

    @Shadow
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano) {
    }

    /**
     * Redirect method to edit the renderWorldPass method.
     */
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorldPass(IFJ)V", ordinal = 0))
    public void redirectRenderWorld0(EntityRenderer renderer,
                                     int pass, float partialTicks, long finishTimeNano, // sub vars
                                     float partialTicksMain, long finishTimeNanoMain // main vars
    ) {
        if (TickRate.runTickRate) {
            rend(0, getRenderTicks(), TickRate.timerWorld.renderPartialTicks, finishTimeNanoMain);
        } else {
            renderWorldPass(0, partialTicksMain, finishTimeNanoMain);
        }
    }

    /**
     * Redirect method to edit the renderWorldPass method.
     */
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorldPass(IFJ)V", ordinal = 1))
    public void redirectRenderWorld1(EntityRenderer renderer,
                                     int pass, float partialTicks, long finishTimeNano, // sub vars
                                     float partialTicksMain, long finishTimeNanoMain // main vars
    ) {
        if (TickRate.runTickRate) {
            rend(1, getRenderTicks(), TickRate.timerWorld.renderPartialTicks, finishTimeNanoMain);
        } else {
            renderWorldPass(1, partialTicksMain, finishTimeNanoMain);
        }
    }

    /**
     * Redirect method to edit the renderWorldPass method.
     */
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorldPass(IFJ)V", ordinal = 2))
    public void redirectRenderWorld2(EntityRenderer renderer,
                                     int pass, float partialTicks, long finishTimeNano, // sub vars
                                     float partialTicksMain, long finishTimeNanoMain // main vars
    ) {
        if (TickRate.runTickRate) {
            rend(2, getRenderTicks(), TickRate.timerWorld.renderPartialTicks, finishTimeNanoMain);
        } else {
            renderWorldPass(2, partialTicksMain, finishTimeNanoMain);
        }
    }

    /**
     * A getter for the timer object in Minecraft.java.
     *
     * @return returns the timer object.
     */
    private float getRenderTicks() {
        return ((IMixinMinecraft) Minecraft.getMinecraft()).getTimer().renderPartialTicks;
    }

    /**
     * A variant of the renderWorldPass method instead of running the original method in EntityRenderer.java
     */
    private void rend(int pass, float partialTicks, float worldTicks, long finishTimeNano) {
        RenderGlobal renderglobal = this.mc.renderGlobal;
        ParticleManager particlemanager = this.mc.effectRenderer;
        boolean flag = this.isDrawBlockOutline();
        GlStateManager.enableCull();
        this.mc.mcProfiler.endStartSection("clear");
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        this.updateFogColor(partialTicks);
        GlStateManager.clear(16640);
        this.mc.mcProfiler.endStartSection("camera");
        this.setupCameraTransform(partialTicks, pass);
        ActiveRenderInfo.updateRenderInfo(this.mc.player, this.mc.gameSettings.thirdPersonView == 2);
        this.mc.mcProfiler.endStartSection("frustum");
        ClippingHelperImpl.getInstance();
        this.mc.mcProfiler.endStartSection("culling");
        ICamera icamera = new Frustum();
        Entity entity = this.mc.getRenderViewEntity();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        icamera.setPosition(d0, d1, d2);

        double lx = entity.lastTickPosX;
        double ly = entity.lastTickPosY;
        double lz = entity.lastTickPosZ;

        if (this.mc.gameSettings.renderDistanceChunks >= 4) {
            this.setupFog(-1, partialTicks);
            this.mc.mcProfiler.endStartSection("sky");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
            GlStateManager.matrixMode(5888);
            renderglobal.renderSky(partialTicks, pass);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * MathHelper.SQRT_2);
            GlStateManager.matrixMode(5888);
        }

        this.setupFog(0, partialTicks);
        GlStateManager.shadeModel(7425);

        if (entity.posY + (double) entity.getEyeHeight() < 128.0D) {
            this.renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
        }

        this.mc.mcProfiler.endStartSection("prepareterrain");
        this.setupFog(0, partialTicks);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        this.mc.mcProfiler.endStartSection("terrain_setup");
        renderglobal.setupTerrain(entity, (double) partialTicks, icamera, this.frameCount++, this.mc.player.isSpectator());

        if (pass == 0 || pass == 2) {
            this.mc.mcProfiler.endStartSection("updatechunks");
            this.mc.renderGlobal.updateChunks(finishTimeNano);
        }

        this.mc.mcProfiler.endStartSection("terrain");
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        renderglobal.renderBlockLayer(BlockRenderLayer.SOLID, (double) partialTicks, pass, entity);
        GlStateManager.enableAlpha();
        renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, (double) partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT, (double) partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);

        if (!this.debugView) {
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("entities");

            entity.lastTickPosX = entity.posX - (entity.posX - lx) * (1.0 - partialTicks) / (1.0 - worldTicks);
            entity.lastTickPosY = entity.posY - (entity.posY - ly) * (1.0 - partialTicks) / (1.0 - worldTicks);
            entity.lastTickPosZ = entity.posZ - (entity.posZ - lz) * (1.0 - partialTicks) / (1.0 - worldTicks);

            renderglobal.renderEntities(entity, icamera, worldTicks);

            entity.lastTickPosX = lx;
            entity.lastTickPosY = ly;
            entity.lastTickPosZ = lz;

            RenderHelper.disableStandardItemLighting();
            this.disableLightmap();
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();

        if (flag && this.mc.objectMouseOver != null && !entity.isInsideOfMaterial(Material.WATER)) {
            EntityPlayer entityplayer = (EntityPlayer) entity;
            GlStateManager.disableAlpha();
            this.mc.mcProfiler.endStartSection("outline");
            renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, partialTicks);
            GlStateManager.enableAlpha();
        }

        entity.lastTickPosX = entity.posX - (entity.posX - lx) * (1.0 - partialTicks) / (1.0 - worldTicks);
        entity.lastTickPosY = entity.posY - (entity.posY - ly) * (1.0 - partialTicks) / (1.0 - worldTicks);
        entity.lastTickPosZ = entity.posZ - (entity.posZ - lz) * (1.0 - partialTicks) / (1.0 - worldTicks);

        if (this.mc.debugRenderer.shouldRender()) {
            this.mc.debugRenderer.renderDebug(partialTicks, finishTimeNano);
        }

        this.mc.mcProfiler.endStartSection("destroyProgress");
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), entity, worldTicks);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableBlend();

        if (!this.debugView) {
            this.enableLightmap();
            this.mc.mcProfiler.endStartSection("litParticles");
            particlemanager.renderLitParticles(entity, worldTicks);
            RenderHelper.disableStandardItemLighting();
            this.setupFog(0, worldTicks);
            this.mc.mcProfiler.endStartSection("particles");
            particlemanager.renderParticles(entity, worldTicks);
            this.disableLightmap();
        }

        entity.lastTickPosX = lx;
        entity.lastTickPosY = ly;
        entity.lastTickPosZ = lz;

        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
        this.mc.mcProfiler.endStartSection("weather");
        this.renderRainSnow(worldTicks);
        GlStateManager.depthMask(true);
        renderglobal.renderWorldBorder(entity, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        this.setupFog(0, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);
        this.mc.mcProfiler.endStartSection("translucent");
        renderglobal.renderBlockLayer(BlockRenderLayer.TRANSLUCENT, (double) partialTicks, pass, entity);
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        if (entity.posY + (double) entity.getEyeHeight() >= 128.0D) {
            this.mc.mcProfiler.endStartSection("aboveClouds");
            this.renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
        }

        this.mc.mcProfiler.endStartSection("hand");

        if (this.renderHand) {
            GlStateManager.clear(256);
            this.renderHand(partialTicks, pass);
        }
    }

    /**
     * fixes the world being culled while noclipping
     */
    @Redirect(method = "renderWorldPass(IFJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z"))
    private boolean fixSpectator(EntityPlayerSP player) {
        return player.isSpectator() || (Config.creativeModeNoClip && player.isCreative());
    }
}
