package com.example.fakelag.mixin;

import com.example.fakelag.FakeLagMod;
import com.example.fakelag.core.PacketDelayQueue;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.Connection")
public class ConnectionInboundMixin {

    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptInbound(ChannelHandlerContext ctx,
                                  Packet<?> packet,
                                  CallbackInfo ci) {
        if (!FakeLagMod.isActive()) return;
        int delay = FakeLagMod.getConfig().inboundDelayMs;
        if (delay <= 0) return;
        ci.cancel();
        PacketDelayQueue.delay(
            () -> ctx.executor().execute(() -> ctx.fireChannelRead(packet)),
            delay
        );
    }
}
