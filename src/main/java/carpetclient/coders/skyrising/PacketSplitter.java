/**
 * This is code provided by skyrising for the use of sending large packet sizes then Minecraft usually allows.
 * Ask skyrising for permition before using this code.
 */

package carpetclient.coders.skyrising;

import com.mumfrey.liteloader.core.ClientPluginChannels;
import com.mumfrey.liteloader.core.PluginChannels;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public class PacketSplitter {
    public static final int MAX_TOTAL_PER_PACKET = 32767;
    public static final int MAX_PAYLOAD_PER_PACKET = MAX_TOTAL_PER_PACKET - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE = Integer.MAX_VALUE;

    private static final Map<String, ReadingSession> readingSessions = new HashMap<>();

    public static boolean send(String channel, PacketBuffer packet, PluginChannels.ChannelPolicy policy) {
        int len = packet.writerIndex();
        packet.resetReaderIndex();
        for (int offset = 0; offset < len; offset += MAX_PAYLOAD_PER_PACKET) {
            int thisLen = Math.min(len - offset, MAX_PAYLOAD_PER_PACKET);
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer(thisLen));
            buf.resetWriterIndex();
            if (offset == 0) buf.writeVarInt(len);
            buf.writeBytes(packet, thisLen);
            if (!ClientPluginChannels.sendMessage(channel, buf, policy)) {
                packet.release();
                return false;
            }
        }
        packet.release();
        return true;
    }

    public static PacketBuffer receive(String channel, PacketBuffer data) {
        return receive(channel, data, DEFAULT_MAX_RECEIVE_SIZE);
    }

    public static PacketBuffer receive(String channel, PacketBuffer data, int maxLength) {
        return readingSessions.computeIfAbsent(channel, ReadingSession::new).receive(data, maxLength);
    }

    private static class ReadingSession {
        private final String key;
        private int expectedSize = -1;
        private PacketBuffer received;

        private ReadingSession(String key) {
            this.key = key;
        }

        private PacketBuffer receive(PacketBuffer data, int maxLength) {
            if (expectedSize < 0) {
                expectedSize = data.readVarInt();
                if (expectedSize > maxLength) throw new IllegalArgumentException("Payload too large");
                received = new PacketBuffer(Unpooled.buffer(expectedSize));
            }
            received.writeBytes(data.readBytes(data.readableBytes()));
            if (received.writerIndex() >= expectedSize) {
                readingSessions.remove(key);
                return received;
            }
            return null;
        }
    }
}