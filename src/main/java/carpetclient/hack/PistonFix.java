package carpetclient.hack;

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

public class PistonFix {
    private static PistonFix instance;
    private static boolean pushPlayersNow;
    private static boolean pistonFix;
    private static boolean firstPistonPush;

    static {
        instance = new PistonFix();
    }

    public static void processPacket(PacketBuffer data) {
        if (pistonFix) {
            instance.fixTileEntitys();
        }
        pistonFix = true;
    }

    public static void movePlayer() {
        if (pushPlayersNow && firstPistonPush) {
            instance.move();
            firstPistonPush = false;
        }
    }

    public static void resetBools() {
        firstPistonPush = true;
        pistonFix = false;
    }

    private void move() {
        Minecraft.getMinecraft().player.onUpdate();
    }

    private void fixTileEntitys() {
        World world = Minecraft.getMinecraft().world;
        Iterator<TileEntity> iterator = world.tickableTileEntities.iterator();
        pushPlayersNow = true;

        while (iterator.hasNext()) {
            TileEntity tileentity = iterator.next();

            if (!(tileentity instanceof TileEntityPiston)) continue;

            if (!tileentity.isInvalid() && tileentity.hasWorld()) {
                BlockPos blockpos = tileentity.getPos();

//                if (world.isBlockLoaded(blockpos) && world.worldBorder.contains(blockpos))
                if (world.isBlockLoaded(blockpos)) {
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
