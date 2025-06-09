package net.facdev.compactedmod;

import net.facdev.compactedmod.command.CompactCommand;
import net.facdev.compactedmod.event.PlayerDeathHandler;
import net.facdev.compactedmod.event.PlayerSpawnHandler;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(CompactedMod.MOD_ID)
public class CompactedMod {
    public static final String MOD_ID = "compacted_mod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompactedMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the player spawn handler
        NeoForge.EVENT_BUS.register(new PlayerSpawnHandler());

        // Register the player death handler
        NeoForge.EVENT_BUS.register(new PlayerDeathHandler());

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CompactCommand.register(event.getDispatcher());
        LOGGER.info("Registered /compact command");
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}