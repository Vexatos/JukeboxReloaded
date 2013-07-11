package net.cazzar.mods.jukeboxreloaded.network.packets;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import net.cazzar.mods.jukeboxreloaded.lib.Reference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;

import static net.cazzar.mods.jukeboxreloaded.lib.Reference.PacketsIDs.*;

public abstract class PacketJukebox {
    public static class ProtocolException extends Exception {

        private static final long serialVersionUID = 4758559873161416283L;

        public ProtocolException() {
        }

        public ProtocolException(String message) {
            super(message);
        }

        public ProtocolException(String message, Throwable cause) {
            super(message, cause);
        }

        public ProtocolException(Throwable cause) {
            super(cause);
        }
    }

    private static final BiMap<Integer, Class<? extends PacketJukebox>> idMap;

    static {
        final ImmutableBiMap.Builder<Integer, Class<? extends PacketJukebox>> builder = ImmutableBiMap
                .builder();

        builder.put(JUKEBOX_DATA, PacketJukeboxDescription.class);
        builder.put(CLIENT_UPDATE_TILE_JUKEBOX, PacketUpdateClientTile.class);
        builder.put(SERVER_SHUFFLE_DISK, PacketShuffleDisk.class);
        builder.put(PLAY_RECORD, PacketPlayRecord.class);
        builder.put(STOP_RECORD, PacketStopPlaying.class);
        builder.put(STOP_ALL, PacketStopAllSounds.class);

        idMap = builder.build();
    }

    public static PacketJukebox constructPacket(int packetId)
            throws ProtocolException, InstantiationException,
            IllegalAccessException {
        final Class<? extends PacketJukebox> clazz = idMap.get(Integer
                .valueOf(packetId));
        if (clazz == null) throw new ProtocolException("Unknown Packet Id!");
        else return clazz.newInstance();
    }

    public abstract void execute(EntityPlayer player, Side side)
            throws ProtocolException;

    public final int getPacketId() {
        if (idMap.inverse().containsKey(getClass())) return idMap.inverse()
                .get(getClass()).intValue();
        else throw new RuntimeException("Packet " + getClass().getSimpleName()
                + " is missing a mapping!");
    }

    public final Packet makePacket() {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(getPacketId());
        write(out);
        return PacketDispatcher.getPacket(Reference.CHANNEL_NAME,
                out.toByteArray());
    }

    public abstract void read(ByteArrayDataInput in);

    public void sendToAllInDimension(int dimID) {
        PacketDispatcher.sendPacketToAllInDimension(makePacket(), dimID);
    }

    public void sendToAllInDimension(Player player) {
        PacketDispatcher.sendPacketToPlayer(makePacket(), player);
    }

    public void sendToAllPlayers() {
        PacketDispatcher.sendPacketToAllPlayers(makePacket());
    }

    public void sendToServer() {
        PacketDispatcher.sendPacketToServer(makePacket());
    }

    public abstract void write(ByteArrayDataOutput out);
}
