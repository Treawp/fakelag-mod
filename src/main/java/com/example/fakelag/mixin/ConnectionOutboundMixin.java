package com.example.fakelag.mixin;

import com.example.fakelag.FakeLagMod;
import com.example.fakelag.config.FakeLagConfig;
import com.example.fakelag.core.PacketDelayQueue;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.Connection")
public class ConnectionOutboundMixin {

    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptOutbound(Packet<?> packet,
                                   PacketSendListener listener,
                                   CallbackInfo ci) {
        if (!FakeLagMod.isActive()) return;
        FakeLagConfig cfg = FakeLagMod.getConfig();
        Connection self = (Connection)(Object) this;

        if (cfg.mode == FakeLagConfig.Mode.PULSE) {
            boolean held = PacketDelayQueue.tryHold(self, packet, listener);
            if (held) ci.cancel();
            return;
        }

        int delay = cfg.outboundDelayMs;
        if (delay <= 0) return;
        ci.cancel();
        if (cfg.logPackets)
            FakeLagMod.LOGGER.info("[OUT] {} +{}ms", packet.getClass().getSimpleName(), delay);
        PacketDelayQueue.delay(() -> self.send(packet, listener), delay);
    }
}
