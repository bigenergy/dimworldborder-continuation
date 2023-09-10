package uk.me.joeclack.dimensionalworldborder;

import com.mojang.logging.LogUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.stream.Collectors;

import static uk.me.joeclack.dimensionalworldborder.DimensionalWorldBorder.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MOD_ID)
public class DimensionalWorldBorder
{
    // Directly reference a slf4j logger
    public static final String MOD_ID = "dimensionalworldborder";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String NETWORK_PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> NETWORK_PROTOCOL_VERSION,
            NETWORK_PROTOCOL_VERSION::equals,
            NETWORK_PROTOCOL_VERSION::equals
    );

    public DimensionalWorldBorder()
    {
        // Register the setup method for modloading
        // FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        // FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        // FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        int networkId = 0;
        NETWORK_CHANNEL.registerMessage(networkId++,
                ClientboundInitialiseDimensionalBorderPacket.class,
                ClientboundInitialiseDimensionalBorderPacket::encode,
                ClientboundInitialiseDimensionalBorderPacket::decode,
                ClientboundInitialiseDimensionalBorderPacket::handle
        );
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // Some example code to dispatch IMC to another mod
        // InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // Some example code to receive and process InterModComms from other mods
        // LOGGER.info("Got IMC {}", event.getIMCStream().
        //        map(m->m.messageSupplier().get()).
        //        collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        DimensionalWBCommand.register(event.getDispatcher());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

        LOGGER.info("Starting Dimensional Worldborder...");

        // Do something when the server starts
        LOGGER.info("Clearing overworld border listeners...");

        MinecraftServer server = event.getServer();
        WorldBorder overworldBorder = server.overworld().getWorldBorder();
        overworldBorder.listeners.clear();

        LOGGER.info("Registering own world border listeners...");
        for (ServerLevel level: server.getAllLevels())
        {
            this.addWorldborderListener(level);
            DimensionalWBSavedData.get(level).applyTo(level.getWorldBorder());
        }

        LOGGER.info("Successfully completed server-starting setup.");

        if (ModList.get().isLoaded("twilightforest")) {
            LOGGER.info("Attempt to fix TF border...");
            int performCommand = event.getServer().getCommands().performPrefixedCommand(
                    event.getServer().createCommandSourceStack(),
                    "/dimworldborder twilightforest:twilight_forest center 0 0"
            );

            if (performCommand == 1) {
                LOGGER.info("Fix applied!");
            }
        }
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save event) {
        LevelAccessor levelAccessor = event.getLevel();
        try {
            assert levelAccessor instanceof ServerLevel;
        }
        catch (AssertionError error) {
            return;
        }
        ServerLevel level = (ServerLevel) levelAccessor;
        //LOGGER.info("Saving worldborder settings for '" + level.dimension().location().toString() + "'...");
        DimensionalWBSavedData.get(level).setWorldborderSettings(level.getWorldBorder().createSettings());
    }

    public void broadcastToAllLevelPlayers(ServerLevel level, Packet<?> packet)
    {
        for (ServerPlayer player: level.players())
        {
            player.connection.send(packet);
        }
    }

    public void addWorldborderListener(ServerLevel level)
    {
        level.getWorldBorder().addListener(new BorderChangeListener()
        {
            public void onBorderSizeSet(WorldBorder border, double size) {
                DimensionalWorldBorder.this.broadcastToAllLevelPlayers(level, new ClientboundSetBorderSizePacket(border));
            }

            public void onBorderSizeLerping(WorldBorder border, double p_11329_, double p_11330_, long p_11331_) {
                DimensionalWorldBorder.this.broadcastToAllLevelPlayers(level, new ClientboundSetBorderLerpSizePacket(border));
            }

            public void onBorderCenterSet(WorldBorder border, double centerX, double centerZ) {
                DimensionalWorldBorder.this.broadcastToAllLevelPlayers(level, new ClientboundSetBorderCenterPacket(border));
            }

            public void onBorderSetWarningTime(WorldBorder border, int time) {
                DimensionalWorldBorder.this.broadcastToAllLevelPlayers(level, new ClientboundSetBorderWarningDelayPacket(border));
            }

            public void onBorderSetWarningBlocks(WorldBorder border, int blocks) {
                DimensionalWorldBorder.this.broadcastToAllLevelPlayers(level, new ClientboundSetBorderWarningDistancePacket(border));
            }

            public void onBorderSetDamagePerBlock(WorldBorder border, double perBlockDamage) {
            }

            public void onBorderSetDamageSafeZOne(WorldBorder border, double damageSafeZone) {
            }
        });
    }
}
