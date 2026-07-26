package com.crazerium.infohud.network;

import com.crazerium.infohud.InfoHUD;
import com.crazerium.infohud.network.packet.ServerStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private static int nextPacketId = 0;
    private static boolean registered = false;

    private static final SimpleChannel CHANNEL =
            NetworkRegistry.ChannelBuilder
                    .named(
                            new ResourceLocation(
                                    InfoHUD.MOD_ID,
                                    "main"
                            )
                    )
                    .networkProtocolVersion(
                            () -> PROTOCOL_VERSION
                    )
                    .clientAcceptedVersions(
                            PROTOCOL_VERSION::equals
                    )
                    .serverAcceptedVersions(
                            PROTOCOL_VERSION::equals
                    )
                    .simpleChannel();

    private ModNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        CHANNEL.messageBuilder(
                        ServerStatsPacket.class,
                        nextPacketId++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ServerStatsPacket::encode)
                .decoder(ServerStatsPacket::new)
                .consumerMainThread(ServerStatsPacket::handle)
                .add();
    }

    public static void sendToPlayer(
            ServerPlayer player,
            ServerStatsPacket packet
    ) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                packet
        );
    }
}