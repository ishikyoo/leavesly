package com.ishikyoo.iyoo.state.property;

import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BitsmartRegistry {
    private static final Map<Identifier, BitsmartProperty> BY_ID = new ConcurrentHashMap<>();
    private static final Map<IntProperty, BitsmartProperty> BY_PROPERTY = new ConcurrentHashMap<>();

    private BitsmartRegistry() {
        throw new UnsupportedOperationException("BitsmartRegistry is a static utility class");
    }

    public static BitsmartProperty create(Identifier id, int capacity) {
        Objects.requireNonNull(id, "Identifier cannot be null");
        if (BY_ID.containsKey(id)) {
            throw new IllegalArgumentException("BitsmartProperty already registered with id: " + id);
        }

        BitsmartProperty property = new BitsmartProperty(id, capacity);
        BY_ID.put(id, property);
        BY_PROPERTY.put(property.property(), property);
        return property;
    }

    public static BitsmartProperty register(BitsmartProperty property) {
        Objects.requireNonNull(property, "Property cannot be null");

        Identifier id = property.identifier();

        if (BY_ID.containsKey(id)) {
            throw new IllegalArgumentException("BitsmartProperty already registered with id: " + id);
        }

        BY_ID.put(id, property);
        BY_PROPERTY.put(property.property(), property);
        return property;
    }

    public static BitsmartProperty get(Identifier id) {
        return BY_ID.get(Objects.requireNonNull(id, "Identifier cannot be null"));
    }

    public static boolean contains(IntProperty property) {
        return BY_PROPERTY.containsKey(property);
    }

    public static boolean contains(Identifier identifier) {
        return BY_ID.containsKey(identifier);
    }

    public static BitsmartProperty get(IntProperty property) {
        return BY_PROPERTY.get(Objects.requireNonNull(property, "Property cannot be null"));
    }
}
