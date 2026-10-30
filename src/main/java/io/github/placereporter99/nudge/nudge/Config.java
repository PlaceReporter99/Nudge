package io.github.placereporter99.nudge.nudge;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@Mod(value = Nudge.MODID, dist = Dist.CLIENT)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.DoubleValue DISTANCE = BUILDER
            .translation("nudge.config.distance")
            .comment("How many blocks forward you will move with each press of the key.")
            .defineInRange("nudgeDistance", 1, (double) 0, 100);
    public static final ModConfigSpec.BooleanValue IGNORE_Y = BUILDER
            .translation("nudge.config.ignorey")
            .comment("Whether to ignore the up/down angle of your head or not. If this is activated along with the other toggle setting, this constrains the keys to the rigid XYZ axis.")
            .define("nudgeIgnoreUpDown", false);
    public static final ModConfigSpec.BooleanValue IGNORE_XZ = BUILDER
            .translation("nudge.config.ignorexz")
            .comment("Whether to ignore the left/right angle of your head or not. If this is activated along with the other toggle setting, this constrains the keys to the rigid XYZ axis.")
            .define("nudgeIgnoreLeftRight", false);
    public static final ModConfigSpec.BooleanValue NEGATE_FALL = BUILDER
            .translation("nudge.config.nofall")
            .comment("Whether to allow air jumping and also cancel fall damage while using Nudge. May burn hunger points.")
            .define("nudgeNegateFall", true);
    static final ModConfigSpec SPEC = BUILDER.build();
}
