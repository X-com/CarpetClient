package carpetclient.mixinInterface;

/**
 * Duck interface for MixinTimer.java
 */
public interface AMixinTimer {
    int getElapsedTicksPlayer();
    // float getRenderPartialTicksWorld();
    float getRenderPartialTicksPlayer();
    void setRenderPartialTicksWorld(float value);
    void setRenderPartialTicksPlayer(float value);

    void setWorldTickRate(float tps);
    float getWorldTickRate();
    float getPlayerTickRate();
}
