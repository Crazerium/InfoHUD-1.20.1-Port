package com.crazerium.infohud.server;

import com.crazerium.infohud.InfoHUD;
import com.crazerium.infohud.network.ModNetwork;
import com.crazerium.infohud.network.packet.ServerStatsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(
        modid = InfoHUD.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ServerStatsEvents {

    private static final int SAMPLE_COUNT = 100;
    private static final int SYNC_INTERVAL_TICKS = 20;

    private static final double[] TICK_TIMES =
            new double[SAMPLE_COUNT];

    private static long tickStartedNanos;

    private static int sampleIndex;
    private static int collectedSamples;
    private static int syncTimer;

    private ServerStatsEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {
        if (event.phase == TickEvent.Phase.START) {
            tickStartedNanos = System.nanoTime();
            return;
        }

        if (tickStartedNanos == 0L) {
            return;
        }

        double currentMspt =
                (System.nanoTime() - tickStartedNanos)
                        / 1_000_000.0D;

        TICK_TIMES[sampleIndex] = currentMspt;

        sampleIndex =
                (sampleIndex + 1) % SAMPLE_COUNT;

        if (collectedSamples < SAMPLE_COUNT) {
            collectedSamples++;
        }

        syncTimer++;

        if (syncTimer < SYNC_INTERVAL_TICKS) {
            return;
        }

        syncTimer = 0;

        MinecraftServer server =
                ServerLifecycleHooks.getCurrentServer();

        if (server == null) {
            return;
        }

        double averageMspt = calculateAverageMspt();

        double tps = averageMspt <= 0.0D
                ? 20.0D
                : Math.min(
                20.0D,
                1000.0D / averageMspt
        );

        Runtime runtime = Runtime.getRuntime();

        long usedMemoryMiB = toMiB(
                runtime.totalMemory()
                        - runtime.freeMemory()
        );

        long maxMemoryMiB = toMiB(
                runtime.maxMemory()
        );

        for (
                ServerPlayer player
                : server.getPlayerList().getPlayers()
        ) {
            int jumps = player.getStats().getValue(
                    Stats.CUSTOM.get(Stats.JUMP)
            );

            int playTimeTicks =
                    player.getStats().getValue(
                            Stats.CUSTOM.get(
                                    Stats.PLAY_TIME
                            )
                    );

            ModNetwork.sendToPlayer(
                    player,
                    new ServerStatsPacket(
                            tps,
                            averageMspt,
                            usedMemoryMiB,
                            maxMemoryMiB,
                            jumps,
                            playTimeTicks
                    )
            );
        }
    }

    private static double calculateAverageMspt() {
        if (collectedSamples == 0) {
            return 0.0D;
        }

        double total = 0.0D;

        for (
                int index = 0;
                index < collectedSamples;
                index++
        ) {
            total += TICK_TIMES[index];
        }

        return total / collectedSamples;
    }

    private static long toMiB(long bytes) {
        return bytes / 1024L / 1024L;
    }
}