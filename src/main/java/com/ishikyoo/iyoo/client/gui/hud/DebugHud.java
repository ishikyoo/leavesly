package com.ishikyoo.iyoo.client.gui.hud;

import com.ishikyoo.iyoo.state.BitsmartStateViewer;
import com.ishikyoo.iyoo.state.property.BitsmartProperty;
import com.ishikyoo.iyoo.state.property.BitsmartRegistry;
import com.ishikyoo.iyoo.state.property.SubProperty;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.RaycastContext;

import java.util.*;

public final class DebugHud {
    private static final String COLOR_DEFAULT = "§f";
    private static final String COLOR_RED = "§c";
    private static final String COLOR_GREEN = "§a";

    private static final String SUFFIX_VALUE = ": ";
    private static final String SUFFIX_PROPERTY = ", ";

    private static final Set<String> VANILLA_PROPERTY_NAMES = Set.of(
            "age", "age_1", "age_2", "age_3", "axis", "berries", "bites", "bottom",
            "buttons", "candles", "charge", "charges", "chiseled", "coast_direction",
            "color", "conditional", "cracked", "delay", "disarmed", "distance", "down",
            "drag", "east", "egg_count", "enabled", "extended", "eye", "face", "facing",
            "falling", "false", "flowers", "half", "has_book", "has_bottle_0",
            "has_bottle_1", "has_bottle_2", "has_record", "has_sculk_sensor", "hinge",
            "honey_level", "inverted", "in_wall", "layers", "leaves", "level", "level_0",
            "level_1", "lit", "locked", "mode", "moisture", "north", "note", "occupied",
            "open", "orientation", "part", "persistent", "pickles", "powered", "power",
            "rotation", "sandwich", "shape", "short", "shriek", "signal_fire",
            "slot_0_occupied", "slot_1_occupied", "slot_2_occupied", "slot_a_occupied",
            "slot_b_occupied", "slot_c_occupied", "snowy", "south", "stage",
            "structure_block_type", "suspicious", "thickness", "tilt", "triggered",
            "type", "unstable", "up", "vertical_direction", "vine_direction",
            "waterlogged", "west"
    );

    private DebugHud() {
        // Prevent instantiation
    }

    public static List<String> getRightText(List<String> original) {
        MinecraftClient client = MinecraftClient.getInstance();

        BlockHitResult hit = raycastBlock(20.0);

        if (hit != null && client.world != null) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = client.world.getBlockState(pos);

            List<String> filtered = new ArrayList<>();
            boolean vanillaPropertiesAppended = false;

            for (String line : original) {
                int colonIndex = line.indexOf(":");
                if (colonIndex < 0) {
                    filtered.add(line);
                    continue;
                }

                String linePropertyName = line.substring(0, colonIndex).trim();
                Property<?> matchedProperty = getPropertyByName(state, linePropertyName);

                if (matchedProperty != null) {
                    if (VANILLA_PROPERTY_NAMES.contains(linePropertyName)) {
                        if (!vanillaPropertiesAppended) {
                            String vanillaProps = getVanillaPropertyString(state);
                            if (vanillaProps != null) {
                                filtered.add(vanillaProps);
                            }
                            vanillaPropertiesAppended = true;
                        }
                    } else if (matchedProperty instanceof IntProperty intProp) {
                        BitsmartProperty bitsmart = BitsmartRegistry.get(intProp);
                        if (bitsmart != null && bitsmart.name().equals(matchedProperty.getName())) {
                            BitsmartStateViewer viewer = BitsmartStateViewer.of(state, bitsmart);
                            filtered.add(getBitsmartPropertyString(viewer));
                            filtered.add(getLightsString(client.world, pos));
                        } else {
                            filtered.add(line);
                        }
                    } else {
                        filtered.add(line);
                    }
                } else {
                    filtered.add(line);
                }
            }
            return filtered;
        }

        return original;
    }

    private static Property<?> getPropertyByName(BlockState state, String name) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    private static String getVanillaPropertyString(BlockState state) {
        StringBuilder builder = new StringBuilder("minecraft [");
        boolean hasProperties = false;

        for (Property<?> property : state.getProperties()) {
            String propName = property.getName();
            if (VANILLA_PROPERTY_NAMES.contains(propName)) {
                Object value = state.get(property);
                if (hasProperties) {
                    builder.append(SUFFIX_PROPERTY);
                }
                builder.append(propName).append(SUFFIX_VALUE).append(formatValue(property, value));
                hasProperties = true;
            }
        }

        builder.append("]");
        return hasProperties ? builder.toString() : null;
    }

    private static String getBitsmartPropertyString(BitsmartStateViewer viewer) {
        StringBuilder builder = new StringBuilder();
        BitsmartProperty property = viewer.property();
        builder.append(property.identifier()).append(" [");

        boolean hasProperties = false;
        for (SubProperty<?> sub : property.getSubProperties()) {
            Object value = viewer.get(sub);
            if (hasProperties) {
                builder.append(SUFFIX_PROPERTY);
            }
            builder.append(sub.name()).append(SUFFIX_VALUE).append(formatValue(sub, value));
            hasProperties = true;
        }

        builder.append("]");
        return builder.toString();
    }

//    private static String getUnknownPropertyString(BlockState state) {
//        StringBuilder builder = new StringBuilder("unknown [");
//        boolean hasProperties = false;
//
//        for (Property<?> property : state.getProperties()) {
//            String propName = property.name();
//            if (!VANILLA_PROPERTY_NAMES.contains(propName)) {
//                if (property instanceof IntProperty intProp && BitsmartRegistry.contains(intProp)) {
//                    continue;
//                }
//                Object value = state.get(property);
//                if (hasProperties) {
//                    builder.append(SUFFIX_PROPERTY);
//                }
//                builder.append(propName).append(SUFFIX_VALUE).append(formatValue(property, value));
//                hasProperties = true;
//            }
//        }
//
//        builder.append("]");
//        return hasProperties ? builder.toString() : null;
//    }

    private static String formatValue(Property<?> property, Object value) {
        if (property.getType().equals(Boolean.class)) {
            return (Boolean) value ? COLOR_GREEN + "true" + COLOR_DEFAULT : COLOR_RED + "false" + COLOR_DEFAULT;
        } else if (property.getType().equals(Integer.class)) {
            return value.toString();
        } else if (property.getType().isEnum()) {
            return value.toString().toLowerCase(Locale.ROOT);
        }
        return value.toString();
    }

    private static String formatValue(SubProperty<?> subProperty, Object value) {
        if (subProperty.isBoolean()) {
            return (Boolean) value ? COLOR_GREEN + "true" + COLOR_DEFAULT : COLOR_RED + "false" + COLOR_DEFAULT;
        } else if (subProperty.isInteger()) {
            return value.toString();
        } else if (subProperty.isEnum()) {
            return value.toString().toLowerCase(Locale.ROOT);
        }
        return value.toString();
    }

    private static BlockHitResult raycastBlock(double maxDistance) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.cameraEntity == null) {
            return null;
        }

        Entity camera = client.cameraEntity;
        Vec3d start = camera.getCameraPosVec(1.0F);
        Vec3d direction = camera.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(maxDistance));

        BlockHitResult result = client.world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                camera
        ));

        return result.getType() == HitResult.Type.BLOCK ? result : null;
    }

    private static String getLightsString(ClientWorld world, BlockPos position) {
        StringBuilder builder = new StringBuilder();
        builder.append("light [sky: ").
                append(world.getLightLevel(LightType.SKY, position)).
                append(", block: ").
                append(world.getLightLevel(LightType.BLOCK, position)).
                append("]");
        return builder.toString();
    }
}
