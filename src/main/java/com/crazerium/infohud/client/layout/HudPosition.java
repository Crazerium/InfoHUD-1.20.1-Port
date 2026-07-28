package com.crazerium.infohud.client.layout;

public record HudPosition(double x, double y) {
    public HudPosition clamped() {
        return new HudPosition(clamp(this.x), clamp(this.y));
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}