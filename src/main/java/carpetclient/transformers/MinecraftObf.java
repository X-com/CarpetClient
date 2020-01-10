package carpetclient.transformers;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Provide necessary obfuscation mapping at run time
 */

@Mixin(Minecraft.class)
@SuppressWarnings("unused")
public abstract class MinecraftObf {
    private static Minecraft __TARGET;

    private void runTickObf() throws Exception {
        __TARGET.runTick();
    }

    @Shadow
    private @Final Timer timer;
    private Object timerObf() {
        return this.timer;
    }

    @Shadow
    private int rightClickDelayTimer;
    private int rightClickDelayTimerObf() {
        return this.rightClickDelayTimer;
    }

    private Object ingameGUIObf() {
        return __TARGET.ingameGUI;
    }

    private Object playerControllerObf() {
        return __TARGET.playerController;
    }

    @Shadow
    private TextureManager renderEngine;
    private Object renderEngineObf() {
        return this.renderEngine;
    }

    private void displayGuiScreenObf() {
        __TARGET.displayGuiScreen(null);
    }

    @Shadow
    private int leftClickCounter;
    private int leftClickCounterObf() {
        return this.leftClickCounter;
    }

    private Object currentScreenObf() {
        return __TARGET.currentScreen;
    }

    @Shadow
    private void runTickMouse() throws IOException {};
    private void runTickMouseObf() throws IOException {
        this.runTickMouse();
    }

    @Shadow
    private void runTickKeyboard() throws IOException {};
    private void runTickKeyboardObf() throws IOException {
        this.runTickKeyboard();
    }

    @Shadow
    private int joinPlayerCounter;
    private int joinPlayerCounterObf() {
        return this.joinPlayerCounter;
    }

    private Object worldObf() {
        return __TARGET.world;
    }

    private Object renderGlobalObf() {
        return __TARGET.renderGlobal;
    }

    @Shadow
    private MusicTicker musicTicker;
    private Object musicTickerObf() {
        return this.musicTicker;
    }

    @Shadow
    private SoundHandler soundHandler;
    private Object soundHandlerObf() {
        return this.soundHandler;
    }

    private Object effectRendererObf() {
        return __TARGET.effectRenderer;
    }

    @Shadow
    private NetworkManager networkManager;
    private Object networkManagerObf() {
        return this.networkManager;
    }
}
