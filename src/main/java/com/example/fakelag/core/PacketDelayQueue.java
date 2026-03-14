package com.example.fakelag.core;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PacketDelayQueue {

    private static final ScheduledExecutorService SCHEDULER =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FakeLag-Scheduler");
            t.setDaemon(true);
            return t;
        });

    public record HeldPacket(
        Connection connection,
        Packet<?> packet,
        PacketSendListener listener
    ) {}

    private static final List<HeldPacket> HOLD_QUEUE = new ArrayList<>();
    private static final Object LOCK = new Object();
    private static ScheduledFuture<?> pulseTask = null;
    private static volatile boolean holding = false;

    public static void delay(Runnable callback, long delayMs) {
        if (delayMs <= 0) { callback.run(); return; }
        SCHEDULER.schedule(callback, delayMs, TimeUnit.MILLISECONDS);
    }

    public static void startPulse(long holdMs, long releaseMs) {
        stopPulse();
        holding = true;
        schedulePulseLoop(holdMs, releaseMs);
    }

    private static void schedulePulseLoop(long holdMs, long releaseMs) {
        pulseTask = SCHEDULER.schedule(() -> {
            holding = false;
            flushQueue();
            pulseTask = SCHEDULER.schedule(() -> {
                holding = true;
                schedulePulseLoop(holdMs, releaseMs);
            }, releaseMs, TimeUnit.MILLISECONDS);
        }, holdMs, TimeUnit.MILLISECONDS);
    }

    public static boolean tryHold(Connection connection,
                                  Packet<?> packet,
                                  PacketSendListener listener) {
        synchronized (LOCK) {
            if (!holding) return false;
            HOLD_QUEUE.add(new HeldPacket(connection, packet, listener));
            return true;
        }
    }

    public static void flushQueue() {
        List<HeldPacket> toFlush;
        synchronized (LOCK) {
            if (HOLD_QUEUE.isEmpty()) return;
            toFlush = new ArrayList<>(HOLD_QUEUE);
            HOLD_QUEUE.clear();
        }
        for (HeldPacket hp : toFlush) {
            hp.connection().send(hp.packet(), hp.listener());
        }
    }

    public static void stopPulse() {
        if (pulseTask != null) pulseTask.cancel(false);
        holding = false;
        flushQueue();
    }

    public static void shutdown() {
        stopPulse();
        SCHEDULER.shutdownNow();
    }
            }
