package uk.me.joeclack.dimensionalworldborder.mixins;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(at=@At("HEAD"), method="handleInitializeBorder(Lnet/minecraft/network/protocol/game/ClientboundInitializeBorderPacket;)V", cancellable = true)
    public void handleInitializeBorderPacket(ClientboundInitializeBorderPacket packet, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}