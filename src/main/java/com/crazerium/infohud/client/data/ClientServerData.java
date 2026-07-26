package com.crazerium.infohud.client.data;

public final class ClientServerData {

    private static final long STALE_AFTER_MILLIS = 5000L;

    private static double tps;
    private static double mspt;

    private static long serverUsedMemoryMiB;
    private static long serverMaxMemoryMiB;

    private static int jumps;
    private static int playTimeTicks;

    private static long lastUpdateMillis;

    private ClientServerData() {
    }

    public static void update(
            double newTps,
            double newMspt,
            long newServerUsedMemoryMiB,
            long newServerMaxMemoryMiB,
            int newJumps,
            int newPlayTimeTicks
    ) {
        tps = newTps;
        mspt = newMspt;

        serverUsedMemoryMiB = newServerUsedMemoryMiB;
        serverMaxMemoryMiB = newServerMaxMemoryMiB;

        jumps = newJumps;
        playTimeTicks = newPlayTimeTicks;

        lastUpdateMillis = System.currentTimeMillis();
    }

    public static boolean hasFreshData() {
        return lastUpdateMillis != 0L
                && System.currentTimeMillis() - lastUpdateMillis
                <= STALE_AFTER_MILLIS;
    }

    public static double getTps() {
        return tps;
    }

    public static double getMspt() {
        return mspt;
    }

    public static long getServerUsedMemoryMiB() {
        return serverUsedMemoryMiB;
    }

    public static long getServerMaxMemoryMiB() {
        return serverMaxMemoryMiB;
    }

    public static int getJumps() {
        return jumps;
    }

    public static int getPlayTimeTicks() {
        return playTimeTicks;
    }

    public static void clear() {
        tps = 0.0D;
        mspt = 0.0D;

        serverUsedMemoryMiB = 0L;
        serverMaxMemoryMiB = 0L;

        jumps = 0;
        playTimeTicks = 0;

        lastUpdateMillis = 0L;
    }
}