package uk.me.joeclack.dimensionalworldborder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ClientboundInitialiseDimensionalBorderPacket extends ClientboundInitializeBorderPacket {
    private final String dimensionIdentifier;

    public ClientboundInitialiseDimensionalBorderPacket(ServerLevel level, WorldBorder border) {
        super(border);
        this.dimensionIdentifier = level.dimension().location().toString();
    }

    public ClientboundInitialiseDimensionalBorderPacket(FriendlyByteBuf buffer) {
        super(buffer);
        this.dimensionIdentifier = buffer.readUtf();
    }

    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeUtf(this.dimensionIdentifier);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle(final Supplier<NetworkEvent.Context> contextSupplier) {
        try {
            assert Minecraft.getInstance().level != null;
        }
        catch (AssertionError error) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        String clientLevelIdentifier = level.dimension().location().toString();
        if (!clientLevelIdentifier.equals(this.dimensionIdentifier)) {
            return;
        }

        WorldBorder worldborder = level.getWorldBorder();
        worldborder.setCenter(this.getNewCenterX(), this.getNewCenterZ());
        long i = this.getLerpTime();
        if (i > 0L) {
            worldborder.lerpSizeBetween(this.getOldSize(), this.getNewSize(), i);
        } else {
            worldborder.setSize(this.getNewSize());
        }

        worldborder.setAbsoluteMaxSize(this.getNewAbsoluteMaxSize());
        worldborder.setWarningBlocks(this.getWarningBlocks());
        worldborder.setWarningTime(this.getWarningTime());
    }

    public static void encode(final ClientboundInitialiseDimensionalBorderPacket packet, final FriendlyByteBuf buffer) {
        packet.write(buffer);
    }

    public static ClientboundInitialiseDimensionalBorderPacket decode(final FriendlyByteBuf buffer) {
        return new ClientboundInitialiseDimensionalBorderPacket(buffer);
    }

    public static void handle(ClientboundInitialiseDimensionalBorderPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> message.handle(contextSupplier))
        );
        contextSupplier.get().setPacketHandled(true);
    }

    public String getDimensionalIdentifier() {
        return this.dimensionIdentifier;
    }

}
