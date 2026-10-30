package io.github.placereporter99.nudge.nudge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(value = Nudge.MODID, dist = Dist.CLIENT)
public class Nudge {
    public static final String MODID = "nudge";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Lazy<KeyMapping> NUDGE_FORWARD = Lazy.of(() -> new KeyMapping(
            "nudge.keymap.forward",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            KeyMapping.CATEGORY_MOVEMENT
    ));
    private static final Lazy<KeyMapping> NUDGE_BACKWARD = Lazy.of(() -> new KeyMapping(
            "nudge.keymap.backward",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            KeyMapping.CATEGORY_MOVEMENT
    ));
    private static final Lazy<KeyMapping> NUDGE_LEFT = Lazy.of(() -> new KeyMapping(
            "nudge.keymap.left",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT,
            KeyMapping.CATEGORY_MOVEMENT
    ));
    private static final Lazy<KeyMapping> NUDGE_RIGHT = Lazy.of(() -> new KeyMapping(
            "nudge.keymap.right",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT,
            KeyMapping.CATEGORY_MOVEMENT
    ));
    private static final Lazy<KeyMapping> NUDGE_UP = Lazy.of(() -> new KeyMapping(
            "nudge.keymapup",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            KeyMapping.CATEGORY_MOVEMENT
    ));
    private static final Lazy<KeyMapping> NUDGE_DOWN = Lazy.of(() -> new KeyMapping(
            "nudge.keymap.down",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            KeyMapping.CATEGORY_MOVEMENT
    ));
    public Nudge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::bindings);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
    public void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("This is PlaceReporter99's Nudge mod.");
    }
    public void bindings(RegisterKeyMappingsEvent event) {
        event.register(NUDGE_FORWARD.get());
        event.register(NUDGE_BACKWARD.get());
        event.register(NUDGE_LEFT.get());
        event.register(NUDGE_RIGHT.get());
        event.register(NUDGE_UP.get());
        event.register(NUDGE_DOWN.get());
    }
    private static void doDelta(Vec3 thingy) {
        var player = Minecraft.getInstance().player;
        var yRot = player.getYRot() * Math.PI / 180; // XZ // THETA
        var xRot = player.getXRot() * Math.PI / 180; // Y  // PHI
        var pos = player.getPosition(1);
        var delta = thingy;
        if (Config.IGNORE_Y.isFalse()) {
            delta = delta.xRot((float) -xRot);
        }
        if (Config.IGNORE_XZ.isFalse()) {
            delta = delta.yRot((float) -yRot);
        }
        delta = delta.scale(Config.DISTANCE.getAsDouble());
        var newPos = pos.add(delta);
        player.setPos(newPos);
    }
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        while (NUDGE_FORWARD.get().consumeClick()) {
            doDelta(new Vec3(0, 0, 1));
        }
        while (NUDGE_BACKWARD.get().consumeClick()) {
            doDelta(new Vec3(0, 0, -1));
        }
        while (NUDGE_LEFT.get().consumeClick()) {
            doDelta(new Vec3(1, 0, 0));
        }
        while (NUDGE_RIGHT.get().consumeClick()) {
            doDelta(new Vec3(-1, 0, 0));
        }
        while (NUDGE_UP.get().consumeClick()) {
            doDelta(new Vec3(0, 1, 0));
        }
        while (NUDGE_DOWN.get().consumeClick()) {
            doDelta(new Vec3(0, -1, 0));
        }
        if (Config.NEGATE_FALL.isTrue()) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                Minecraft.getInstance().player.setOnGround(true);
            }
        }
    }
}
