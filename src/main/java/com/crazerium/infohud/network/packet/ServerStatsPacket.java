package com.crazerium.infohud.network.packet;

import com.crazerium.infohud.client.data.ClientServerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerStatsPacket(
        double tps,
        double mspt,
        long serverUsedMemoryMiB,
        long serverMaxMemoryMiB,
        int jumps,
        int playTimeTicks
) {

    public ServerStatsPacket(FriendlyByteBuf buffer) {
        this(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeDouble(tps);
        buffer.writeDouble(mspt);

        buffer.writeLong(serverUsedMemoryMiB);
        buffer.writeLong(serverMaxMemoryMiB);

        buffer.writeVarInt(jumps);
        buffer.writeVarInt(playTimeTicks);
    }

    public static void handle(
            ServerStatsPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        ClientServerData.update(
                packet.tps(),
                packet.mspt(),
                packet.serverUsedMemoryMiB(),
                packet.serverMaxMemoryMiB(),
                packet.jumps(),
                packet.playTimeTicks()
        );

        context.setPacketHandled(true);
    }
}