package uk.me.joeclack.dimensionalworldborder.mixins;

import uk.me.joeclack.dimensionalworldborder.ClientboundInitialiseDimensionalBorderPacket;
import uk.me.joeclack.dimensionalworldborder.DimensionalWorldBorder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerList.class)
public class MixinPlayerList {
    @Inject(at=@At("TAIL"), method="sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V", locals= LocalCapture.CAPTURE_FAILSOFT)
    public void sendLevelInfo(ServerPlayer player, ServerLevel level, CallbackInfo callbackInfo, WorldBorder worldborder) {
        WorldBorder otherWorldBorder = level.getWorldBorder();
        DimensionalWorldBorder.NETWORK_CHANNEL.sendTo(new ClientboundInitialiseDimensionalBorderPacket(level, otherWorldBorder), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @Inject(at=@At("HEAD"), method="addWorldborderListener(Lnet/minecraft/server/level/ServerLevel;)V", cancellable = true)
    public void addWorldborderListener(ServerLevel level, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}
