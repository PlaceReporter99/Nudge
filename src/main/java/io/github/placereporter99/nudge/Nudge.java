package io.github.placereporter99.nudge;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            "nudge.keymap.up",
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
    private static final Lazy<KeyMapping> FREEZE = Lazy.of(() -> new KeyMapping(
            "nudge.keymap.freeze",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_APOSTROPHE,
            KeyMapping.CATEGORY_MOVEMENT
    ));
    private static final Lazy<KeyMapping> SUICIDE = Lazy.of(() -> new KeyMapping(
            "nudge.keymap.suicide",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DELETE,
            KeyMapping.CATEGORY_GAMEPLAY
    ));
    private static final List<Map.Entry<Lazy<KeyMapping>, Runnable>> keybinds = new ArrayList<>();
    private static void addKeybind(Lazy<KeyMapping> key, Runnable function) {
        keybinds.add(Map.entry(key, function));
    }
    private static void runKeybinds() {
        for (Map.Entry<Lazy<KeyMapping>, Runnable> entry : keybinds) {
            while (entry.getKey().get().consumeClick()) {
                entry.getValue().run();
            }
        }
    }
    private static Vec3 freezePosition = null;
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
        event.register(FREEZE.get());
        event.register(SUICIDE.get());
    }
    static {
        addKeybind(NUDGE_FORWARD, () -> doDelta(new Vec3(0, 0, 1)));
        addKeybind(NUDGE_BACKWARD, () -> doDelta(new Vec3(0, 0, -1)));
        addKeybind(NUDGE_LEFT, () -> doDelta(new Vec3(1, 0, 0)));
        addKeybind(NUDGE_RIGHT, () -> doDelta(new Vec3(-1, 0, 0)));
        addKeybind(NUDGE_UP, () -> doDelta(new Vec3(0, 1, 0)));
        addKeybind(NUDGE_DOWN, () -> doDelta(new Vec3(0, -1, 0)));
        addKeybind(FREEZE, () -> {
            if (freezePosition == null) {
                freezePosition = Minecraft.getInstance().player.getPosition(1);
            } else {
                freezePosition = null;
            }
        });
        addKeybind(SUICIDE, () -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var pos = player.getPosition(1);
                for (var i = 0; i < 80; i++) {
                    sendMovePacket(pos.add(0, 10, 0), false);
                    sendMovePacket(pos.add(0, 0.05, 0), false);
                }
                sendMovePacket(pos, true);
                player.respawn();
            }
        });
    }
    private static void sendMovePacket(double x, double y, double z, boolean onGround) {
        Minecraft.getInstance().getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, onGround));
    }
    private static void sendMovePacket(Vec3 pos, boolean onGround) {
        sendMovePacket(pos.x, pos.y, pos.z, onGround);
    }
    private static void doDelta(Vec3 thingy) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        var yRot = player.getYRot() * Math.PI / 180; // XZ // THETA
        var xRot = player.getXRot() * Math.PI / 180; // Y  // PHI
        Vec3 pos;
        if (freezePosition == null) {
            pos = player.getPosition(1);
        } else {
            pos = freezePosition;
        }
        var delta = thingy;
        if (Config.IGNORE_Y.isFalse()) {
            delta = delta.xRot((float) -xRot);
        }
        if (Config.IGNORE_XZ.isFalse()) {
            delta = delta.yRot((float) -yRot);
        }
        delta = delta.scale(Config.DISTANCE.getAsDouble());
        var newPos = pos.add(delta);
        if (freezePosition == null) {
            player.setPos(newPos);
        } else {
            freezePosition = newPos;
        }
    }
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            runKeybinds();
        }
        if (freezePosition != null && player != null) {
            player.setPos(freezePosition);
        }
        if (Config.NEGATE_FALL.isTrue()) {
            if (player != null) {
                player.setOnGround(true);
            }
        }
    }
}
