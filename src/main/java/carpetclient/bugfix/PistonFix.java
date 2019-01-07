package carpetclient.bugfix;

import carpetclient.Config;
import carpetclient.mixins.IMixinWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;

/*
Class used to fix players glitching through moving piston blocks.
 */
public class PistonFix {
    private static PistonFix instance;
    private static boolean pushPlayersNow;
    private static boolean pistonFix;
    private static boolean firstPistonPush;

    static {
        instance = new PistonFix();
    }

    /**
     * Process the packet received from the server. Used to synch packets form the server ticks relative to the client ticks.
     *
     * @param data
     */
    public static void processPacket(PacketBuffer data) {
        if (Config.clipThroughPistons) return;

        if (pistonFix) {
            instance.fixTileEntitys();
        }
        pistonFix = true;
    }

    /**
     * Updates player being moved to simulate regular game logic where players move before tile entitys.
     */
    public static void movePlayer() {
        if (pushPlayersNow && firstPistonPush) {
            instance.move();
            firstPistonPush = false;
        }
    }

    /**
     * Resets booleans used in packet synching.
     */
    public static void resetBools() {
        firstPistonPush = true;
        pistonFix = false;
    }

    /**
     * Simulates moving the player
     */
    private void move() {
        Minecraft.getMinecraft().player.onUpdate();
    }

    /**
     * Simulates tile entity's and fast updates them. As many packets can arrive on the client at the same time desynched
     * tick wise relative to the server the only way to fix it is to simulate tile entity's moving within synching packet's.
     */
    private void fixTileEntitys() {
        World world = Minecraft.getMinecraft().world;
        Iterator<TileEntity> iterator = world.tickableTileEntities.iterator();
        pushPlayersNow = true;

        while (iterator.hasNext()) {
            TileEntity tileentity = iterator.next();

            if (!(tileentity instanceof TileEntityPiston)) continue;

            if (!tileentity.isInvalid() && tileentity.hasWorld()) {
                BlockPos blockpos = tileentity.getPos();

                if (world.isBlockLoaded(blockpos) && ((IMixinWorld) world).getWorldBorder().contains(blockpos)) {
                    try {
                        ((ITickable) tileentity).update();
                    } catch (Throwable throwable) {
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory2);
                        throw new ReportedException(crashreport2);
                    }
                }
            }

            if (tileentity.isInvalid()) {
                iterator.remove();
                world.loadedTileEntityList.remove(tileentity);

                if (world.isBlockLoaded(tileentity.getPos())) {
                    world.getChunkFromBlockCoords(tileentity.getPos()).removeTileEntity(tileentity.getPos());
                }
            }
        }

        pushPlayersNow = false;
    }
}
