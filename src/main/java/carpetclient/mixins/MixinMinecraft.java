package carpetclient.mixins;

import carpetclient.Config;
import carpetclient.bugfix.PistonFix;
import carpetclient.rules.TickRate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
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
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tick rate editing in Minecraft.java based on Cubitecks tick rate mod.
 */
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
    Profiler profiler;
    @Shadow
    public EntityPlayerSP player;
    @Shadow
    private int joinPlayerCounter;
    @Shadow
    public EntityRenderer entityRenderer;
    @Shadow
    public RenderGlobal renderGlobal;
    @Shadow
    private SoundHandler soundHandler;
    @Shadow
    private MusicTicker musicTicker;
    @Shadow
    private @Final
    Tutorial tutorial;
    @Shadow
    public ParticleManager effectRenderer;
    @Shadow
    private NetworkManager networkManager;
    @Shadow
    long systemTime;
    @Shadow
    public GuiIngame ingameGUI;
    @Shadow
    public GuiScreen currentScreen;
    @Shadow
    public boolean inGameHasFocus;

    @Shadow
    public static long getSystemTime() {
        return 0;
    }

    @Shadow
    public void setIngameFocus() {
    }

    /**
     * Inject method to place a world timer update method next to the regular timer update. Disabled when tick speeds are synched.
     */
    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;timer:Lnet/minecraft/util/Timer;", shift = At.Shift.BEFORE))
    public void injectWorldTimer(CallbackInfo ci) {
        if (TickRate.runTickRate) {
            TickRate.timerWorld.updateTimer();
        }
    }

    /**
     * Redirect method edit the updateCameraAndRender with the world timer instead of the regular timer. Disabled when tick speeds are synched
     */
    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;updateCameraAndRender(FJ)V"))
    public void redirectUpdateCameraAndRender(EntityRenderer entityRenderer,
                                              float partialTicks, long nanoTime // updateCameraAndRender() vars
                                              // runGameLoop() vars
    ) {
        if (TickRate.runTickRate) {
            entityRenderer.updateCameraAndRender(this.isGamePaused ? this.renderPartialTicksPaused : TickRate.timerWorld.renderPartialTicks, System.nanoTime());
        } else {
            entityRenderer.updateCameraAndRender(this.isGamePaused ? this.renderPartialTicksPaused : timer.renderPartialTicks, System.nanoTime());
        }
    }

    /**
     * Inject after the runTick method have looped to update world and player entities differently. Disabled when tick speeds are synched.
     */
    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;profiler:Lnet/minecraft/profiler/Profiler;", ordinal = 3, shift = At.Shift.BEFORE))
    public void injectPlayerWorldLoops(CallbackInfo ci) {
        if (TickRate.runTickRate) {
            int playerTicks = this.timer.elapsedTicks;
            int worldTicks = TickRate.timerWorld.elapsedTicks;
            while (playerTicks > 0) {
                this.runTickPlayer();
                playerTicks--;
            }
            while (worldTicks > 0) {
                this.runTickWorld();
                worldTicks--;
            }
        }
    }

    /**
     * Reset logic for clipping through pistons.
     *
     * @param ci
     */
    @Inject(method = "runTick", at = @At("HEAD"))
    public void fixingPistons(CallbackInfo ci) {
        PistonFix.resetBools();
    }

    /**
     * Inject to eject out of the run tick method for more granular editing of world and player entities. Disabled when tick speeds are synched.
     */
    @Inject(method = "runTick", at = @At(value = "JUMP", opcode = Opcodes.IFNULL, ordinal = 9, shift = At.Shift.BEFORE), cancellable = true)
    public void injectJumpOutForWorldUpdate(CallbackInfo ci) {
        if (TickRate.runTickRate) {
            ci.cancel();
        }
    }

    /**
     * Modify constant in scroll mouse to fix the issue when slowing down.
     */
    @ModifyConstant(method = "runTickMouse", constant = @Constant(longValue = 200L))
    private long runTickMouseFix(long value) {
        if (TickRate.runTickRate) {
            return (long) Math.max(200F * (20.0f / Config.tickRate), 200L);
        } else {
            return 200L;
        }
    }

    /**
     * Updating player updates at regular speed at 20 ticks per second.
     */
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

            this.profiler.endStartSection("gameRenderer");

            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }
        } else if (this.entityRenderer.isShaderActive()) {
            this.entityRenderer.stopUseShader();
        }
    }

    /**
     * Update world entities and rest of run tick method at servers tick rate.
     */
    public void runTickWorld() {
        if (this.world != null) {
            if (this.player != null) {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30) {
                    this.joinPlayerCounter = 0;
                    this.world.joinEntityInSurroundings(this.player);
                }
            }

            this.profiler.endStartSection("gameRenderer");

            if (!this.isGamePaused) {
                this.entityRenderer.updateRenderer();
            }

            this.profiler.endStartSection("levelRenderer");

            if (!this.isGamePaused) {
                this.renderGlobal.updateClouds();
            }

            this.profiler.endStartSection("level");

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
            this.musicTicker.update();
            this.soundHandler.update();
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

            this.profiler.endStartSection("animateTick");

            if (!this.isGamePaused && this.world != null) {
                this.world.doVoidFogParticles(MathHelper.floor(this.player.posX), MathHelper.floor(this.player.posY), MathHelper.floor(this.player.posZ));
            }

            this.profiler.endStartSection("particles");

            if (!this.isGamePaused) {
                this.effectRenderer.updateEffects();
            }
        } else if (this.networkManager != null) {
            this.profiler.endStartSection("pendingConnection");
            this.networkManager.processReceivedPackets();
        }

        this.profiler.endSection();
        this.systemTime = Minecraft.getSystemTime();
    }
}
