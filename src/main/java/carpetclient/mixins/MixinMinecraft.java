package carpetclient.mixins;

import carpetclient.coders.Cubitect.TickRate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.network.NetworkManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Timer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMixinMinecraft {

    @Shadow
    private @Final
    Timer timer;
    @Shadow
    private boolean isGamePaused;
    @Shadow
    private float renderPartialTicksPaused;
    @Shadow
    public WorldClient world;
    @Shadow
    public @Final
    Profiler mcProfiler;
    @Shadow
    public EntityPlayerSP player;
    @Shadow
    private int joinPlayerCounter;
    @Shadow
    public EntityRenderer entityRenderer;
    @Shadow
    public RenderGlobal renderGlobal;
    @Shadow
    private SoundHandler mcSoundHandler;
    @Shadow
    private MusicTicker mcMusicTicker;
    @Shadow
    private @Final
    Tutorial tutorial;
    @Shadow
    public ParticleManager effectRenderer;
    @Shadow
    private NetworkManager myNetworkManager;
    @Shadow
    long systemTime;

//    @Inject(method = "<init>", at = @At(value = "HEAD"))
//    public void initInject(CallbackInfo ci) {
//        timerWorld = new Timer(1.0F);
//    }

    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;timer:Lnet/minecraft/util/Timer;", shift = At.Shift.BEFORE))
    public void injectWorldTimer(CallbackInfo ci) {
        TickRate.timerWorld.updateTimer();
    }

    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;updateCameraAndRender(FJ)V"))
    public void redirectUpdateCameraAndRender(EntityRenderer entityRenderer,
                                              float partialTicks, long nanoTime // updateCameraAndRender() vars
                                              // runGameLoop() vars
    ) {
        entityRenderer.updateCameraAndRender(this.isGamePaused ? this.renderPartialTicksPaused : TickRate.timerWorld.renderPartialTicks, System.nanoTime());
    }

    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;mcProfiler:Lnet/minecraft/profiler/Profiler;", ordinal = 4))
    public void injectPlayerWorldLoops(CallbackInfo ci) {
        int playerTicks = this.timer.elapsedTicks;
        while (playerTicks > 0) {
            this.runTickPlayer();
            playerTicks--;
        }
        int worldTicks = TickRate.timerWorld.elapsedTicks;
        while (worldTicks > 0) {
            this.runTickWorld();
            worldTicks--;
        }
    }

    @Inject(method = "runTick", at = @At(value = "JUMP", opcode = Opcodes.IFNULL, ordinal = 9, shift = At.Shift.BEFORE), cancellable = true)
    public void injectJumpOutForWorldUpdate(CallbackInfo ci) {
        ci.cancel();
    }

    public void runTickPlayer() {
        if (this.world != null) {
            if (this.player != null) {

                if (!this.isGamePaused) this.world.updateEntity(this.player);
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30) {
                    this.joinPlayerCounter = 0;
                    this.world.joinEntityInSurroundings(this.player);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");

            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }
        } else if (this.entityRenderer.isShaderActive()) {
            this.entityRenderer.stopUseShader();
        }
    }

    public void runTickWorld() {
        if (this.world != null) {
            if (this.player != null) {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30) {
                    this.joinPlayerCounter = 0;
                    this.world.joinEntityInSurroundings(this.player);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");

            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }

            this.mcProfiler.endStartSection("levelRenderer");

            if (!this.isGamePaused) {
                this.renderGlobal.updateClouds();
            }

            this.mcProfiler.endStartSection("level");

            if (!this.isGamePaused) {
                if (this.world.getLastLightningBolt() > 0) {
                    this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
                }

                this.world.loadedEntityList.remove(this.player);
                this.world.updateEntities();
                this.world.loadedEntityList.add(this.player);
            }
        } else if (this.entityRenderer.isShaderActive()) {
            this.entityRenderer.stopUseShader();
        }

        if (!this.isGamePaused) {
            this.mcMusicTicker.update();
            this.mcSoundHandler.update();
        }

        if (this.world != null) {
            if (!this.isGamePaused) {
                this.world.setAllowedSpawnTypes(this.world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                this.tutorial.update();

                try {
                    this.world.tick();
                } catch (Throwable throwable2) {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (this.world == null) {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    } else {
                        this.world.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            this.mcProfiler.endStartSection("animateTick");

            if (!this.isGamePaused && this.world != null) {
                this.world.doVoidFogParticles(MathHelper.floor(this.player.posX), MathHelper.floor(this.player.posY), MathHelper.floor(this.player.posZ));
            }

            this.mcProfiler.endStartSection("particles");

            if (!this.isGamePaused) {
                this.effectRenderer.updateEffects();
            }
        } else if (this.myNetworkManager != null) {
            this.mcProfiler.endStartSection("pendingConnection");
            this.myNetworkManager.processReceivedPackets();
        }

        this.mcProfiler.endSection();
        this.systemTime = Minecraft.getSystemTime();
    }
}
